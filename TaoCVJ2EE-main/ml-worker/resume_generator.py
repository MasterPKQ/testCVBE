from resume_data import *
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
from enum import Enum
from pydantic import BaseModel, Field
from typing import Annotated
from langchain.prompts import ChatPromptTemplate, HumanMessagePromptTemplate, SystemMessagePromptTemplate
from langchain_core.language_models.chat_models import BaseChatModel
import spacy
import asyncio
from datetime import datetime

PAGE_DESCRIPTION_LENGTH = 25

class ResumeEntryType(Enum):
    EXPERIENCE = 1
    PROJECT = 0.9
    EXTRA_CURRICULAR = 0.8

class ResumeEntryData(BaseModel):
    type: ResumeEntryType
    data: ProfileExperience | ProfileProject
    score: float = 0.0
    
class ResumeCreationStrategy(BaseModel):
    pages: int = -1
    minExperiences: int = -1
    maxExperiences: int = -1
    minProjects: int = -1
    maxProjects: int = -1
    minExtraCurriculars: int = -1
    maxExtraCurriculars: int = -1

class ResumeCreationOptions(BaseModel):
    '''
    The options for creating a resume
    '''
    addHighlights: bool = False
    addHobbies: bool = False
    addAwards: bool = False
    addSkills: bool = True
    # in lines
    minDescriptionLength: int = 0
    maxDescriptionLength: int = 5
    # min and max experiences, projects, and extra curriculars
    filterStrategy: Annotated[ResumeCreationStrategy | None, Field(default=None)]

class SkillHolder(BaseModel):
    '''
    Holds the skills which are extracted from a job description
    '''
    required: list[str]
    preferred: list[str]
    
class ResumeRewritenInput(BaseModel):
    job_description: str
    resume_entry_description: str
    resume_entry_skills: list[str]

class ResumeGeneratorService:
    '''
    This is the generic service which generates the resume.
    '''
    model: BaseChatModel
    vectorizer = TfidfVectorizer()
    
    def __init__(self, model: BaseChatModel):
        '''
        Initializes the ResumeGeneratorService with the model which is used to generate the resume.
        '''
        self.model = model
    
    # Returns the similarity score between two texts
    def get_similarity_score(self, text1: str, text2: str) -> float:
        tfidf_matrix = self.vectorizer.fit_transform([text1, text2])
        return cosine_similarity(tfidf_matrix[0:1], tfidf_matrix[1:2])[0][0]

    def get_resume_entries(self, profile: Profile) -> list[ResumeEntryData]:
        '''
        Creates a massive resume from the profile, acts as our template
        '''
        # Create a list of resume entry data's populated with the profile's experiences, projects, and extra curriculars
        resumeEntryData = []
        resumeEntryData.extend([ResumeEntryData(type=ResumeEntryType.EXPERIENCE, data=entry, score=0.0) for entry in profile.experiences])
        resumeEntryData.extend([ResumeEntryData(type=ResumeEntryType.PROJECT, data=entry, score=0.0) for entry in profile.projects])
        resumeEntryData.extend([ResumeEntryData(type=ResumeEntryType.EXTRA_CURRICULAR, data=entry, score=0.0) for entry in profile.extraCurriculars])
        return resumeEntryData
   
    # Abstract
    async def extract_skills(self, description: str) -> SkillHolder:
        '''
        Extracts the skills from a job description
        '''
        pass
   
    # Filters the entries in the resume based on relevance 
    def filter_resume(self, resume_entry_data: list[ResumeEntryData], options: ResumeCreationOptions) -> list[ResumeEntryData]:
        '''
        Filters the resume based on the options
        '''
        if (options.filterStrategy is None):
            return resume_entry_data
        
        # Create a list of resume entry data's
        resume_entries: list[ResumeEntryData] = []
        
        strategy = options.filterStrategy
        # -1 means no definitive length for the pages. Add
        if strategy.pages == -1:
            
            # Create the lists of resume entry data's for each type of entry
            resume_experience_entries = []
            resume_project_entries = []
            resume_extra_curricular_entries = []
            
            # Filter the resume based on the min and max experiences
            if strategy.maxExperiences != -1:
                exp_limit = strategy.maxExperiences
            elif strategy.minExperiences != -1:
                exp_limit = strategy.minExperiences
            else:
                exp_limit = 0
                
            # Filter the resume based on the min and max projects
            if strategy.maxProjects != -1:
                proj_limit = strategy.maxProjects
            elif strategy.minProjects != -1:
                proj_limit = strategy.minProjects
            else:
                proj_limit = 0
            
            # Filter the resume based on the min and max extra curriculars
            if strategy.maxExtraCurriculars != -1:
                ec_limit = strategy.maxExtraCurriculars
            elif strategy.minExtraCurriculars != -1:
                ec_limit = strategy.minExtraCurriculars
            else:
                exp_limit = 0
            
            # Filter the resume based on the min and max experiences
            for entry in resume_entry_data:
                if entry.type == ResumeEntryType.EXPERIENCE and len(resume_experience_entries) < exp_limit:
                    resume_experience_entries.append(entry)
                elif entry.type == ResumeEntryType.PROJECT and len(resume_project_entries) < proj_limit:
                    resume_project_entries.append(entry)
                elif entry.type == ResumeEntryType.EXTRA_CURRICULAR and len(resume_extra_curricular_entries) < ec_limit:
                    resume_extra_curricular_entries.append(entry)
            
            # Clear the resume entry data and add the filtered entries
            resume_entries.extend(resume_experience_entries)
            resume_entries.extend(resume_project_entries)
            resume_entries.extend(resume_extra_curricular_entries)
        else:
            # Get the minimum and maximum number of entries based on the number of pages
            min_entries = strategy.pages * (PAGE_DESCRIPTION_LENGTH // options.maxDescriptionLength)
            max_entries = strategy.pages * (PAGE_DESCRIPTION_LENGTH // options.minDescriptionLength)
            num_entries = (min_entries + max_entries) // 2
            
            # Create the lists of resume entry data's for each type of entry
            experiences: list[ResumeEntryData] = []
            projects: list[ResumeEntryData] = []
            extra_curriculars: list[ResumeEntryData] = []
            
            # get the loop limits
            loop_exp = strategy.maxExperiences if strategy.maxExperiences != -1 else len(resume_entry_data)
            loop_proj = strategy.maxProjects if strategy.maxProjects != -1 else len(resume_entry_data)
            loop_ec = strategy.maxExtraCurriculars if strategy.maxExtraCurriculars != -1 else len(resume_entry_data)
            
            # Categorize them
            for entry in resume_entry_data:
                if entry.type == ResumeEntryType.EXPERIENCE:
                    if  len(experiences) < loop_exp:
                        experiences.append(entry)
                elif entry.type == ResumeEntryType.PROJECT:
                    if len(projects) < loop_proj:
                        projects.append(entry)
                elif entry.type == ResumeEntryType.EXTRA_CURRICULAR:
                    if len(extra_curriculars) < loop_ec:
                        extra_curriculars.append(entry)
            
            # get the start indexes
            start_ex = strategy.minExperiences if strategy.minExperiences != -1 else 0
            start_proj = strategy.minProjects if strategy.minProjects != -1 else 0
            start_ec = strategy.minExtraCurriculars if strategy.minExtraCurriculars != -1 else 0
            
            # add the minimum required entries
            resume_entries.extend(experiences[:start_ex])
            resume_entries.extend(projects[:start_proj])
            resume_entries.extend(extra_curriculars[:start_ec])
            
            # add the rest of the entries based on how much is left
            while len(resume_entries) < num_entries:
                # Create a list of entries to check
                entries_to_check: list[ResumeEntryData] = []
                
                # Check if there are any entries to check
                if len(experiences) > start_ex:
                    entries_to_check.append(experiences[start_ex])
                if len(projects) > start_proj:
                    entries_to_check.append(projects[start_proj])
                if len(extra_curriculars) > start_ec:
                    entries_to_check.append(extra_curriculars[start_ec])
                
                # if not enough entries then break
                if len(entries_to_check) == 0:
                    break
                
                # Get the entry with the highest score
                entry = max(entries_to_check, key=lambda x: x.score)
                resume_entries.append(entry)
                # increment the start index of the entry type
                if entry.type == ResumeEntryType.EXPERIENCE:
                    start_ex += 1
                elif entry.type == ResumeEntryType.PROJECT:
                    start_proj += 1
                elif entry.type == ResumeEntryType.EXTRA_CURRICULAR:
                    start_ec += 1
            
        
        return resume_entries
    
    # Gets the skills score
    def get_skill_score(self, skills: list[str], skill_holder: SkillHolder) -> float:
        '''
        Returns the skill score between the skills and the job skills
        '''
        required_skills = skill_holder.required
        preferred_skills = skill_holder.preferred
        # Calculate the required score
        intersection_required = set(skills).intersection(required_skills)
        intersection_preferred = set(skills).intersection(preferred_skills)
        
        # Calculate the required and preferred score
        required_score = len(intersection_required) / len(required_skills) if len(required_skills) != 0 else 1
        preferred_score = len(intersection_preferred) / len(preferred_skills) if len(preferred_skills) != 0 else 1
        
        # Return the weighted score
        return 0.6 * required_score + 0.4 * preferred_score
        
    async def rewrite_description(self, entry: ResumeEntryData, job_description: str, options: ResumeCreationOptions):
        '''
        Rewrites the description to be more similar to the job using Gen AI
        '''
        # create the chat template
        chat_template = ChatPromptTemplate.from_messages([
            SystemMessagePromptTemplate.from_template(
                f"""
                You are a candidate for a job whose description is provided. 
                You need to rewrite your resume entry to be more similar to the job description.
                The details of the input will be sent as a JSON text.
                The job description will be in the "job_description" field.
                The resume entry description will be in the "resume_entry_description" field.
                The skills that the entry contains will be in the "resume_entry_skills" field.
                You will output the job description. The job description should be minimum {options.minDescriptionLength} lines and maximum {options.maxDescriptionLength} lines.
                Each line should be minimum 10 words and maximum 20 words and should be in the XYZ format.
                """
            ),
            HumanMessagePromptTemplate.from_template("{input}"),
        ])
        
        # create the input
        input = ResumeRewritenInput(job_description=job_description,
                                    resume_entry_description='\n'.join(entry.data.description),
                                    resume_entry_skills=[skill.name for skill in entry.data.skills])
        
        class ResumeRewritenOutput(BaseModel):
            updated_resume_entry_description: list[str]
        
        model = self.model.with_structured_output(ResumeRewritenOutput)
        
        # return the rewritten description
        output: ResumeRewritenOutput = await model.ainvoke(chat_template.format_prompt(input=input).to_messages())
        entry.data.description = output.updated_resume_entry_description
    
    async def generate_resume(self, profile: Profile, job: Job, options: ResumeCreationOptions = ResumeCreationOptions()) -> Resume:
        """Generates a resume based off of the profile and the job
    
        Algorithm:
        
        1. Get profile, job, and options
        2. Populate the resume data with the profile's name, contact info, and education
        3. Create a list of resume entry data's populated with the profile's experiences, projects, and extra curriculars
        4. Go through each resume entry data and calculate the similarity score between 
            the resume entry's description and skills and the job description
        5. Sort the resume entry data by the resume entry score
        6. For each resume entry data in the resume data, re-write the description to be more similar to the job using Gen AI 
            then add it to the resume data
        7. Add the resume data and job to the resume and return the resume

        Args:
            profile (Profile): The profile to generate the resume from
            job (Job): The job to generate the resume for
            options (ResumeCreationOptions, optional): The user specified options about the resume. Defaults to ResumeCreationOptions().

        Returns:
            Resume: The generated resume
        """
        current_year = datetime.now().year
        
        # Populate the resume data with the profile's name, contact info, and education
        resumeEntryData = self.get_resume_entries(profile)
        
        # Extract the skills from the job description
        extracted_skills = await self.extract_skills(job.description)
        
        # Go through each resume entry data and calculate the similarity score between the resume entry's description and skills and the job description
        for entry in resumeEntryData:
            end_year = entry.data.duration.end.split("-")[0] == "Present" and current_year or entry.data.duration.end.split("-")[0]
            recency_factor = int(end_year) / current_year
            
            description_score = self.get_similarity_score('\n'.join(entry.data.description), job.description)
            skills_score = self.get_skill_score([skill.name for skill in entry.data.skills], extracted_skills)
            entry.score = entry.type.value * (description_score + skills_score) * recency_factor
        
        # Sort the resume entry data by the resume entry score
        resumeEntryData.sort(key=lambda x: x.score, reverse=True)
        
        # filter the resume
        resumeEntryData = self.filter_resume(resumeEntryData, options)
        
        # For each resume entry data in the resume data, re-write the description to be more similar to the job using Gen AI
        tasks = [self.rewrite_description(entry, job.description, options) for entry in resumeEntryData]
        await asyncio.gather(*tasks)
        
        # Add the resume data and job to the resume and return the resume
        data = ResumeData(
            name=profile.name,
            awards=[],
            contactInfo=profile.contactInfo,
            education=profile.education,
            experiences=[],
            extraCurriculars=[],
            projects=[],
            skills=[],
            highlights=[],
            hobbies=[],
        )
        
        skills: set[Skill] = set([])
        for entry in resumeEntryData:
            # populate the resume data with the profile's experiences, projects, and extra curriculars
            if entry.type == ResumeEntryType.EXPERIENCE:
                data.experiences.append(entry.data)
            elif entry.type == ResumeEntryType.PROJECT:
                data.projects.append(entry.data)
            elif entry.type == ResumeEntryType.EXTRA_CURRICULAR:
                data.extraCurriculars.append(entry.data)
            
            # add skills
            if (options.addSkills):
                skills.update(entry.data.skills)
        
        if (options.addSkills):
            skills.update(profile.otherSkills)
            data.skills += list(skills)
        
        if (options.addHobbies):
            data.hobbies = profile.hobbies
        
        if (options.addAwards):
            data.awards = profile.otherAwards
        
        return Resume(data=data, job=job)
        

class LangChainStrategyResumeGeneratorService(ResumeGeneratorService):
    '''
    Implements the extract_skills method using langchain
    '''

    async def extract_skills(self, description: str) -> SkillHolder:
        '''
        Extracts the skills from a job description
        '''
        chat_template = ChatPromptTemplate.from_messages(
            [
                SystemMessagePromptTemplate.from_template(
                    "You are a candidate for a job whose description is provided. You'll need to extract all the required and preferred technical skills given the job description to see whether your profile is a good fit for the position or not. Your background does not matter as long as all the skills that are required and preferred in the job description are reported back as the response. Required skills go into the required_skills array. Preferred skills would go to the preferred_skills array. Degrees (Bachelors, Masters, PhD) are not skills."
                ),
                HumanMessagePromptTemplate.from_template("{input}"),
            ]
        )
        
        skill_model = self.model.with_structured_output(SkillHolder)
        return await skill_model.ainvoke(chat_template.format_prompt(input=description).to_messages())

class SpaCyStrategyResumeGeneratorService(ResumeGeneratorService):
    '''
    Implements the extract_skills method using langchain
    '''
    nlp = spacy.load("en_core_web_sm")
    required_keywords = ["must", "required", "experience", "should", "expertise"]
    preferred_keywords = ["preferred", "nice to have", "plus"]

    def extract_skills_internal(self, job_description: str) -> SkillHolder:
        '''
        Extracts the skills from a job description
        '''
        # create a document
        doc = self.nlp(job_description)

        # create the sets
        required_skills = set()
        preferred_skills = set()

        # loop through sentences to categorize skills
        for sent in doc.sents:
            context = "neutral"

            # Determine context based on keywords
            for word in sent:
                if word.text.lower() in self.required_keywords:
                    context = "required"
                elif word.text.lower() in self.preferred_keywords:
                    context = "preferred"

            # Extract noun phrases (potential skills)
            for chunk in sent.noun_chunks:
                if context == "required":
                    required_skills.add(chunk.text)
                elif context == "preferred":
                    preferred_skills.add(chunk.text)

        return SkillHolder(required=required_skills, preferred=preferred_skills)
    
    def preprocess_document(self, document: str):
        return document.replace("\n", " ")

    def sanitize_skills(self, skills: set[str]) -> set[str]:
        sanitized_skills = set()

        for skill in skills:
            # Filter out short or overly generic phrases
            if len(skill.split()) < 2 and skill.islower():
                continue

            # Check for stopwords or generic phrases
            doc = self.nlp(skill)
            if any(token.is_stop for token in doc) or "skill" in skill.lower():
                continue

            # Retain proper nouns, nouns, or meaningful phrases
            if any(word.lower() in self.required_keywords for word in skill.split()) or any(word.lower() in self.preferred_keywords for word in skill.split()):
                continue

            if any(token.pos_ in {"NOUN", "PROPN"} for token in doc):
                sanitized_skills.add(skill.strip())

        return sanitized_skills
    
    async def extract_skills(self, job_description: str) -> SkillHolder:
        '''
        Extracts the skills from a job description
        '''
        skill_holder = self.extract_skills_internal(self.preprocess_document(job_description))
        skill_holder.required = list(self.sanitize_skills(skill_holder.required))
        skill_holder.preferred = list(self.sanitize_skills(skill_holder.preferred))
        return skill_holder

if __name__ == "__main__":
    import app_secrets
    import asyncio
    from langchain_google_genai import ChatGoogleGenerativeAI
    
    model = ChatGoogleGenerativeAI(model="gemini-1.5-pro", temperature=0.7, verbose=True)
    resume_generator = LangChainStrategyResumeGeneratorService(model=model)
    
    options = ResumeCreationOptions(
        minDescriptionLength=2,
        maxDescriptionLength=3,
        filterStrategy=ResumeCreationStrategy(
            pages=1,
            minExperiences=1,
        )
    )
    
    profile = Profile(
        name="Aditya Rao",
        contactInfo=ContactInfo(
            mediaProfiles=[
                MediaProfile(platform="Phone", handle="647-978-3730"),
                MediaProfile(platform="Email", handle="raoa32@mcmaster.ca"),
                MediaProfile(platform="LinkedIn", handle="https://linkedin.com/in/aditya-g-rao"),
                MediaProfile(platform="GitHub", handle="https://github.com/adityarao2005"),
                MediaProfile(platform="Portfolio", handle="https://adityarao-portfolio.vercel.app"),
            ]
        ),
        education=[
            Education(
                institution="McMaster University",
                qualification="B.Eng. in Software Engineer (CO-OP)",
                duration=Duration(start="2023-09-01", end="2028-04-30"),
                location=Location(city="Hamilton", country="Canada"),
                description=[
                    "4.00/4.00 CGPA - Dean’s Honor List",
                    "Relevant Courses: Digital Systems, OOP, Discrete Math, Intro to Software Development"
                ],
                courses=[]
            )
        ],
        experiences=[
            ProfileExperience(
                title="Full-stack Developer Intern",
                company="Medium AI",
                description=[
                    "Developed and maintained React and TypeScript code for Medium AI, contributing to 2 first-place and 1 second-place finishes in the McMaster Startup Survivor competition, and helping secure $10,000 in funding.",
                    "Implemented real-time speech transcription using Python, OpenAI Whisper, FFmpeg, gRPC, and Socket.IO",
                    "Updated the React frontend to accommodate realtime transcription as well as test integration with Web APIs and Firebase Realtime Database",
                    "Links: https://theforge.mcmaster.ca/startups/medium-ai/"
                ],
                duration=Duration(start="2024-05-01", end="2024-08-31"),
                location=Location(city="Hamilton", country="Canada"),
                skills=[
                    Skill(name="Typescript", type="Programming Language"),
                    Skill(name="Python", type="Programming Language"),
                    Skill(name="Socket.io", type="Programming Language"),
                    Skill(name="React", type="Programming Language"),
                    Skill(name="Linux", type="Programming Language"),
                    Skill(name="gRPC", type="Programming Language"),
                    Skill(name="OpenAI Whisper", type="Programming Language"),
                    Skill(name="Web APIs", type="Programming Language"),
                    Skill(name="ffmpeg", type="Programming Language"),
                    Skill(name="Firebase Realtime Database", type="Programming Language")
                ]
            )
        ],
        projects=[
            ProfileProject(
                title="Resume Builder",
                duration=Duration(start="2024-08-20", end="Present"),
                description=[
                    "Built a real-time AI-powered resume builder and critiquer for creating, editing, and scoring resumes.",
                    "Secured with Firebase Auth, Java Spring Boot OAuth2, and MongoDB",
                    "Developing ML services with Docker, Kubernetes, Gemini, LangChain, Python, and FastAPI.",
                    "Used Next.js, TailwindCSS, DaisyUI, and TypeScript for the frontend."
                ],
                skills=[
                    Skill(name="Typescript", type="Programming Language"),
                    Skill(name="Next.js", type="Programming Language"),
                    Skill(name="TailwindCSS", type="Programming Language"),
                    Skill(name="DaisyUI", type="Programming Language"),
                    Skill(name="Redux", type="Programming Language"),
                    Skill(name="Python", type="Programming Language"),
                    Skill(name="FastAPI", type="Programming Language"),
                    Skill(name="Docker", type="Programming Language"),
                    Skill(name="Kubernetes", type="Programming Language"),
                    Skill(name="Gemini", type="Programming Language"),
                    Skill(name="LangChain", type="Programming Language"),
                    Skill(name="Spring Boot", type="Programming Language"),
                    Skill(name="MongoDB", type="Programming Language"),
                    Skill(name="Firebase Auth", type="Programming Language"),
                    Skill(name="SciKit Learn", type="Programming Language"),
                    Skill(name="spaCy", type="Programming Language"),
                ]
            ),
            ProfileProject(
                title="FashioNova",
                duration=Duration(start="2024-10-04", end="2024-10-06"),
                description=[
                    "Developed a virtual wardrobe and AI fashion advisor in 36 hours at Hack the Valley 9 at UofT Scarborough",
                    "Created an OpenCV pipeline using Python Flask and Socket.io to fit virtual clothes onto users via live video feed",
                    "Leveraged Cloudflare AI to suggest outfits based on the occasion, promoting sustainable fashion consumption.",
                    "Used Next.js, TailwindCSS, and TypeScript for the frontend.",
                    "Links: https://devpost.com/software/fashionova"
                ],
                skills=[
                    Skill(name="Typescript", type="Programming Language"),
                    Skill(name="Next.js", type="Programming Language"),
                    Skill(name="TailwindCSS", type="Programming Language"),
                    Skill(name="Python", type="Programming Language"),
                    Skill(name="Flask", type="Programming Language"),
                    Skill(name="Socket.io", type="Programming Language"),
                    Skill(name="OpenCV", type="Programming Language"),
                    Skill(name="Cloudflare AI", type="Programming Language"),
                    Skill(name="Computer Vision", type="Programming Language")
                ]
            ),
            ProfileProject(
                title="Smart Studdy Buddy",
                duration=Duration(start="2024-08-15", end="2024-08-17"),
                description=[
                    "Developed an intelligent chatbot capable of summarizing course materials and answering academic queries.",
                    "Integrated OpenAI API for natural language processing to provide general question-answering capabilities.",
                    "Utilized Wolfram Alpha API to handle scientific and computational queries, offering detailed responses.",
                    "Built a feature to generate practice problems based on course material and enhance learning using Python and Flask.",
                    "Used Next.js, TailwindCSS, and TypeScript for the frontend.",
                    "Links: https://github.com/adityarao2005/smart-study-buddy "
                ],
                skills=[
                    Skill(name="Typescript", type="Programming Language"),
                    Skill(name="Next.js", type="Programming Language"),
                    Skill(name="TailwindCSS", type="Programming Language"),
                    Skill(name="Python", type="Programming Language"),
                    Skill(name="Flask", type="Programming Language"),
                    Skill(name="OpenAI API", type="Programming Language"),
                    Skill(name="Wolfram Alpha API", type="Programming Language"),
                ]
            ),
            ProfileProject(
                title="QuakeGuard",
                duration=Duration(start="2024-01-12", end="2024-01-14"),
                description=[
                    "Developed an earthquake prediction tool using ML models showcased at DeltaHacks X and GDSC McMaster.",
                    "Visualized global earthquake data and high-risk areas with Next.js and Python, Flask, SciKit Learn, and TensorFlow.",
                    "Used Next.js, TailwindCSS, and TypeScript for the frontend.",
                    "Links: https://devpost.com/software/quakeguard"
                ],
                skills=[
                    Skill(name="Typescript", type="Programming Language"),
                    Skill(name="Next.js", type="Programming Language"),
                    Skill(name="TailwindCSS", type="Programming Language"),
                    Skill(name="Flask", type="Programming Language"),
                    Skill(name="SciKit Learn", type="Programming Language"),
                    Skill(name="TensorFlow", type="Programming Language"),
                    Skill(name="Keras", type="Programming Language"),
                    Skill(name="Machine Learning", type="Programming Language"),
                    Skill(name="Python", type="Programming Language")
                ]
            )
        ],
        extraCurriculars=[
            ProfileExperience(
                title="Sys-admin, Fullstack Developer, and Club Executive",
                company="IEEE McMaster Student Branch",
                description=[
                    "Set up an RPi 3B+ Cluster for a server with MicroK8s to handle up to 100 clients in an instant with a budget of $5000",
                    "Developing the website for the IEEE student branch and the admin dashboard for executives to manage",
                    "Developing with Typescript, Next.js, Aceterny, PostegreSQL, Docker, Kubernetes",
                    "Links: https://ieeemcmaster.ca"
                ],
                skills=[
                    Skill(name="Typescript", type="Programming Language"),
                    Skill(name="Next.js", type="Programming Language"),
                    Skill(name="PostgreSQL", type="Programming Language"),
                    Skill(name="Docker", type="Programming Language"),
                    Skill(name="Kubernetes", type="Programming Language"),
                    Skill(name="Raspberry Pis", type="Programming Language"),
                ],
                duration=Duration(start="2024-11-15", end="Present"),
                location=Location(city="Hamilton", country="Canada")
            )
        ],
        otherSkills=[
            Skill(name="C", type="Programming Language"),
            Skill(name="C++", type="Programming Language"),
            Skill(name="JavaScript", type="Programming Language"),
            Skill(name="AWS Cloud Practioner", type="Programming Language")
        ],
        hobbies=[],
        otherAwards=[],
    )
    
    job = Job(
        company="Nasdaq",
        title="Software Development Intern",
        duration=Duration(start="2025-05-01", end="2025-08-31"),
        description="""
        About the job
We're seeking a talented Software Development Intern to join our vibrant team.

What You'll Do:

Collaborate with your team, while delivering project requirements in an agile environment
Design solutions to solve technical problems
Participate in code reviews to ensure quality

What We're Looking For:

Must be a rising senior (Dec 2025/Spring 2026 grad) enrolled full time for the upcoming semester in an accredited college or university in Computer Engineering, Software Engineering, Computer Science, or related major and on track to graduate
Strong analytical skills for complex and creative problem solving
Excellent organizational skills
Ability to meet team goals and timelines

What Additional Experience You Could Bring:

Conducting automated testing
Familiarity with big data technologies
Aware of data extraction, manipulation/cleansing and integration processes
Experience with SaaS Software Development
Demonstrated experience and understanding of working within an Agile Framework
Experience and understanding of working within an Agile Practice

What Success Looks Like:

Proactively contributing to team meetings, product days, or other company sessions
Being a constructive team member who is consistently dependable
Delivering high-quality work on assigned projects

This position is located in St. John’s, and offers the opportunity for a hybrid work environment (2 days a week in office), providing flexibility and accessibility for qualified candidates. 

This job posting closes on Tuesday, January 14th, 2025 at 11:59pm Newfoundland Time Zone.

Come as You Are

Nasdaq is an equal opportunity employer. We positively encourage applications from suitably qualified and eligible candidates regardless of age, color, disability, national origin, ancestry, race, religion, gender, sexual orientation, gender identity and/or expression, veteran status, genetic information, or any other status protected by applicable law.

We will ensure that individuals with disabilities are provided reasonable accommodation to participate in the job application or interview process, to perform essential job functions, and to receive other benefits and privileges of employment. Please contact us to request an accommodation.
        """
    )
    
    print("Options")
    print(options)
    
    print()
    print("Profile")
    print(profile)
    
    print()
    print("Job")
    print(job)
    
    async def main():
        print()
        
        print("Resume")
        resume = await resume_generator.generate_resume(profile, job, options)
        print(resume)
        
    asyncio.run(main())
    
    pass

def result_output():
    data=ResumeData(
        name='Aditya Rao',
        contactInfo=ContactInfo(
            address=None,
            mediaProfiles=[
                MediaProfile(platform='Phone', handle='647-978-3730'),
                MediaProfile(platform='Email', handle='raoa32@mcmaster.ca'),
                MediaProfile(platform='LinkedIn', handle='https://linkedin.com/in/aditya-g-rao'),
                MediaProfile(platform='GitHub', handle='https://github.com/adityarao2005'),
                MediaProfile(platform='Portfolio', handle='https://adityarao-portfolio.vercel.app')
            ]
        ),
        highlights=[],
        education=[
            Education(
                qualification='B.Eng. in Software Engineer (CO-OP)',
                institution='McMaster University',
                duration=Duration(start='2023-09-01', end='2028-04-30'),
                location=Location(city='Hamilton', country='Canada'),
                description=[
                    '4.00/4.00 CGPA - Dean’s Honor List',
                    'Relevant Courses: Digital Systems, OOP, Discrete Math, Intro to Software Development'
                ]
            )
        ],
        experiences=[
            ProfileExperience(
                title='Full-stack Developer Intern',
                company='Medium AI',
                description=[
                    'Collaborated with a team in an agile environment to develop and maintain React and TypeScript code for Medium AI, resulting in 2 first-place and 1 second-place finishes in the McMaster Startup Survivor competition and securing $10,000 in funding.',
                    'Designed and implemented real-time speech transcription using Python, OpenAI Whisper, FFmpeg, gRPC, and Socket.IO, demonstrating strong analytical and problem-solving skills.',
                    'Updated the React frontend to accommodate real-time transcription and integrated with Web APIs and Firebase Realtime Database, showcasing experience with SaaS Software Development and delivering high-quality work.'
                ],
                duration=Duration(start='2024-05-01', end='2024-08-31'),
                location=Location(city='Hamilton', country='Canada'),
                skills=[
                    Skill(name='Typescript', type='Programming Language', proficiencyLevel=None),
                    Skill(name='Python', type='Programming Language', proficiencyLevel=None),
                    Skill(name='Socket.io', type='Programming Language', proficiencyLevel=None),
                    Skill(name='React', type='Programming Language', proficiencyLevel=None),
                    Skill(name='Linux', type='Programming Language', proficiencyLevel=None),
                    Skill(name='gRPC', type='Programming Language', proficiencyLevel=None),
                    Skill(name='OpenAI Whisper', type='Programming Language', proficiencyLevel=None),
                    Skill(name='Web APIs', type='Programming Language', proficiencyLevel=None),
                    Skill(name='ffmpeg', type='Programming Language', proficiencyLevel=None),
                    Skill(name='Firebase Realtime Database', type='Programming Language', proficiencyLevel=None)
                ]
            )
        ],
        projects=[
            ProfileProject(
                title='Smart Studdy Buddy',
                description=[
                    'Developed an intelligent chatbot using Next.js, TailwindCSS, and Typescript, integrating OpenAI and Wolfram Alpha APIs for enhanced learning.',
                    'Designed and implemented a feature to generate practice problems based on course material using Python and Flask, demonstrating strong analytical and problem-solving skills.',
                    'Contributed to a project within an agile environment, showcasing collaboration and ability to meet project requirements and deadlines.'
                ],
                duration=Duration(start='2024-08-15', end='2024-08-17'),
                skills=[
                    Skill(name='Typescript', type='Programming Language', proficiencyLevel=None),
                    Skill(name='Next.js', type='Programming Language', proficiencyLevel=None),
                    Skill(name='TailwindCSS', type='Programming Language', proficiencyLevel=None),
                    Skill(name='Python', type='Programming Language', proficiencyLevel=None),
                    Skill(name='Flask', type='Programming Language', proficiencyLevel=None),
                    Skill(name='OpenAI API', type='Programming Language', proficiencyLevel=None),
                    Skill(name='Wolfram Alpha API', type='Programming Language', proficiencyLevel=None)
                ]
            ),
            ProfileProject(
                title='Resume Builder',
                description=[
                    'Developed ML services using Python, Docker, and Kubernetes, collaborating with a team in an agile environment.',
                    'Designed and implemented a real-time AI-powered resume builder and critiquer, contributing to innovative solutions.',
                    'Ensured code quality through rigorous testing and code reviews, leveraging Gemini and LangChain for enhanced performance.'
                ],
                duration=Duration(start='2024-08-20', end='Present'),
                skills=[
                    Skill(name='Typescript', type='Programming Language', proficiencyLevel=None),
                    Skill(name='Next.js', type='Programming Language', proficiencyLevel=None),
                    Skill(name='TailwindCSS', type='Programming Language', proficiencyLevel=None),
                    Skill(name='DaisyUI', type='Programming Language', proficiencyLevel=None),
                    Skill(name='Redux', type='Programming Language', proficiencyLevel=None),
                    Skill(name='Python', type='Programming Language', proficiencyLevel=None),
                    Skill(name='FastAPI', type='Programming Language', proficiencyLevel=None),
                    Skill(name='Docker', type='Programming Language', proficiencyLevel=None), 
                    Skill(name='Kubernetes', type='Programming Language', proficiencyLevel=None),
                    Skill(name='Gemini', type='Programming Language', proficiencyLevel=None), 
                    Skill(name='LangChain', type='Programming Language', proficiencyLevel=None),
                    Skill(name='Spring Boot', type='Programming Language', proficiencyLevel=None), 
                    Skill(name='MongoDB', type='Programming Language', proficiencyLevel=None), 
                    Skill(name='Firebase Auth', type='Programming Language', proficiencyLevel=None),
                    Skill(name='SciKit Learn', type='Programming Language', proficiencyLevel=None), 
                    Skill(name='spaCy', type='Programming Language', proficiencyLevel=None)
                ]
            ),
            ProfileProject(
                title='FashioNova',
                description=[
                    'Collaborated with a team in an agile environment to develop a virtual wardrobe and AI fashion advisor (Hack the Valley 9).',
                    'Designed and implemented a computer vision pipeline using OpenCV, Python Flask, and Socket.io for virtual clothing try-on via live video feed.', 
                    'Leveraged Cloudflare AI to provide occasion-based outfit suggestions, promoting sustainable fashion and demonstrating experience with SaaS Software Development.'
                ], 
                duration=Duration(start='2024-10-04', end='2024-10-06'),
                skills=[
                    Skill(name='Typescript', type='Programming Language', proficiencyLevel=None),
                    Skill(name='Next.js', type='Programming Language', proficiencyLevel=None), 
                    Skill(name='TailwindCSS', type='Programming Language', proficiencyLevel=None),
                    Skill(name='Python', type='Programming Language', proficiencyLevel=None),
                    Skill(name='Flask', type='Programming Language', proficiencyLevel=None),
                    Skill(name='Socket.io', type='Programming Language', proficiencyLevel=None), 
                    Skill(name='OpenCV', type='Programming Language', proficiencyLevel=None), 
                    Skill(name='Cloudflare AI', type='Programming Language', proficiencyLevel=None), 
                    Skill(name='Computer Vision', type='Programming Language', proficiencyLevel=None)
                ]
            ),
            ProfileProject(
                title='QuakeGuard',
                description=[
                    'Developed a machine learning tool showcased at DeltaHacks X and GDSC McMaster, demonstrating strong analytical skills for creative problem-solving.', 
                    'Utilized Next.js, TailwindCSS, and Typescript for frontend development, Python, Flask, SciKit Learn, and TensorFlow for backend and model training, contributing to team goals and meeting project timelines.',
                    'Successfully visualized global earthquake data and identified high-risk areas, showcasing experience with data extraction, manipulation, and integration processes.'
                ],
                duration=Duration(start='2024-01-12', end='2024-01-14'),
                skills=[
                    Skill(name='Typescript', type='Programming Language', proficiencyLevel=None),
                    Skill(name='Next.js', type='Programming Language', proficiencyLevel=None),
                    Skill(name='TailwindCSS', type='Programming Language', proficiencyLevel=None),
                    Skill(name='Flask', type='Programming Language', proficiencyLevel=None), 
                    Skill(name='SciKit Learn', type='Programming Language', proficiencyLevel=None), 
                    Skill(name='TensorFlow', type='Programming Language', proficiencyLevel=None),
                    Skill(name='Keras', type='Programming Language', proficiencyLevel=None), 
                    Skill(name='Machine Learning', type='Programming Language', proficiencyLevel=None), 
                    Skill(name='Python', type='Programming Language', proficiencyLevel=None)
                ]
            )
        ],
        extraCurriculars=[
            ProfileExperience(
                title='Sys-admin, Fullstack Developer, and Club Executive', 
                company='IEEE McMaster Student Branch',
                description=[
                    'Developed a website and admin dashboard using TypeScript, Next.js, PostgreSQL, Docker, and Kubernetes.',
                    'Collaborated with a team in an agile environment to deliver project requirements within a $5000 budget.',
                    'Conducted automated testing and ensured quality through code reviews.'
                ],
                duration=Duration(start='2024-11-15', end='Present'), 
                location=Location(city='Hamilton', country='Canada'), 
                skills=[
                    Skill(name='Typescript', type='Programming Language', proficiencyLevel=None),
                    Skill(name='Next.js', type='Programming Language', proficiencyLevel=None),
                    Skill(name='PostgreSQL', type='Programming Language', proficiencyLevel=None),
                    Skill(name='Docker', type='Programming Language', proficiencyLevel=None),
                    Skill(name='Kubernetes', type='Programming Language', proficiencyLevel=None),
                    Skill(name='Raspberry Pis', type='Programming Language', proficiencyLevel=None)
                ]
            )
        ],
        skills=[
            Skill(name='Typescript', type='Programming Language', proficiencyLevel=None),
            Skill(name='Python', type='Programming Language', proficiencyLevel=None), 
            Skill(name='Socket.io', type='Programming Language', proficiencyLevel=None),
            Skill(name='React', type='Programming Language', proficiencyLevel=None), 
            Skill(name='Linux', type='Programming Language', proficiencyLevel=None),
            Skill(name='gRPC', type='Programming Language', proficiencyLevel=None), 
            Skill(name='OpenAI Whisper', type='Programming Language', proficiencyLevel=None), 
            Skill(name='Web APIs', type='Programming Language', proficiencyLevel=None), 
            Skill(name='ffmpeg', type='Programming Language', proficiencyLevel=None), 
            Skill(name='Firebase Realtime Database', type='Programming Language', proficiencyLevel=None), 
            Skill(name='Typescript', type='Programming Language', proficiencyLevel=None),
            Skill(name='Next.js', type='Programming Language', proficiencyLevel=None),
            Skill(name='TailwindCSS', type='Programming Language', proficiencyLevel=None), 
            Skill(name='Python', type='Programming Language', proficiencyLevel=None), 
            Skill(name='Flask', type='Programming Language', proficiencyLevel=None), 
            Skill(name='OpenAI API', type='Programming Language', proficiencyLevel=None),
            Skill(name='Wolfram Alpha API', type='Programming Language', proficiencyLevel=None),
            Skill(name='Typescript', type='Programming Language', proficiencyLevel=None),
            Skill(name='Next.js', type='Programming Language', proficiencyLevel=None), 
            Skill(name='PostgreSQL', type='Programming Language', proficiencyLevel=None), 
            Skill(name='Docker', type='Programming Language', proficiencyLevel=None),
            Skill(name='Kubernetes', type='Programming Language', proficiencyLevel=None), 
            Skill(name='Raspberry Pis', type='Programming Language', proficiencyLevel=None),
            Skill(name='Typescript', type='Programming Language', proficiencyLevel=None), 
            Skill(name='Next.js', type='Programming Language', proficiencyLevel=None),
            Skill(name='TailwindCSS', type='Programming Language', proficiencyLevel=None), 
            Skill(name='DaisyUI', type='Programming Language', proficiencyLevel=None), 
            Skill(name='Redux', type='Programming Language', proficiencyLevel=None),
            Skill(name='Python', type='Programming Language', proficiencyLevel=None),
            Skill(name='FastAPI', type='Programming Language', proficiencyLevel=None), 
            Skill(name='Docker', type='Programming Language', proficiencyLevel=None),
            Skill(name='Kubernetes', type='Programming Language', proficiencyLevel=None),
            Skill(name='Gemini', type='Programming Language', proficiencyLevel=None), 
            Skill(name='LangChain', type='Programming Language', proficiencyLevel=None),
            Skill(name='Spring Boot', type='Programming Language', proficiencyLevel=None),
            Skill(name='MongoDB', type='Programming Language', proficiencyLevel=None), 
            Skill(name='Firebase Auth', type='Programming Language', proficiencyLevel=None),
            Skill(name='SciKit Learn', type='Programming Language', proficiencyLevel=None),
            Skill(name='spaCy', type='Programming Language', proficiencyLevel=None), 
            Skill(name='Typescript', type='Programming Language', proficiencyLevel=None), 
            Skill(name='Next.js', type='Programming Language', proficiencyLevel=None), 
            Skill(name='TailwindCSS', type='Programming Language', proficiencyLevel=None),
            Skill(name='Python', type='Programming Language', proficiencyLevel=None), 
            Skill(name='Flask', type='Programming Language', proficiencyLevel=None), 
            Skill(name='Socket.io', type='Programming Language', proficiencyLevel=None),
            Skill(name='OpenCV', type='Programming Language', proficiencyLevel=None), 
            Skill(name='Cloudflare AI', type='Programming Language', proficiencyLevel=None), 
            Skill(name='Computer Vision', type='Programming Language', proficiencyLevel=None),
            Skill(name='Typescript', type='Programming Language', proficiencyLevel=None),
            Skill(name='Next.js', type='Programming Language', proficiencyLevel=None),
            Skill(name='TailwindCSS', type='Programming Language', proficiencyLevel=None),
            Skill(name='Flask', type='Programming Language', proficiencyLevel=None), 
            Skill(name='SciKit Learn', type='Programming Language', proficiencyLevel=None), 
            Skill(name='TensorFlow', type='Programming Language', proficiencyLevel=None),
            Skill(name='Keras', type='Programming Language', proficiencyLevel=None),
            Skill(name='Machine Learning', type='Programming Language', proficiencyLevel=None), 
            Skill(name='Python', type='Programming Language', proficiencyLevel=None), 
            Skill(name='C', type='Programming Language', proficiencyLevel=None), 
            Skill(name='C++', type='Programming Language', proficiencyLevel=None),
            Skill(name='JavaScript', type='Programming Language', proficiencyLevel=None), 
            Skill(name='AWS Cloud Practioner', type='Programming Language', proficiencyLevel=None)
        ], 
        awards=[], 
        hobbies=[]
    )