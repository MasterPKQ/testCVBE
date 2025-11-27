
from langchain_core.language_models.chat_models import BaseChatModel
from pydantic import BaseModel, Field
from typing import List
from langchain.prompts import ChatPromptTemplate, HumanMessagePromptTemplate, SystemMessagePromptTemplate
import json
from resume_cleaning_service import clean_resume

class ResumeGradingReport(BaseModel):
    '''    
    This is the output schema of the resume grading service
    this is what is returned to the user
    '''
    score: int = Field(description="The score of the report")
    pros: List[str] = Field(description="The things which the resume has which is good")
    cons: List[str] = Field(description="The things which the resume has which is bad or the things \
     which are good which this resume does not have")
    improvements: List[str] = Field(description="The ways to turn what is bad about the resume into a good resume")

class ResumeGraderService:
    '''
    This is the generic service which grades the resume.
    This is meant to be inherited by the actual service which grades the resume.
    '''
    def grade_resume(self, resume: dict) -> ResumeGradingReport:
        '''
        Grades the resume based on the language model. 
        Note that the resume should be cleaned first before sending it to this function.
        '''
        pass

class LangChainResumeGraderService(ResumeGraderService):
    '''
    This is a generic service which uses langchain to grade the resume.
    The generic part is which LLM model is being used to grade the resume.
    '''
    
    def __init__(self, model: BaseChatModel):
        '''
        Initializes the LangChainResumeGraderService with the model which is used to grade the resume.
        Passes in a LLM model which is used to grade the resume.
        '''
        # The model which is used to grade the resume
        self.model = model.with_structured_output(ResumeGradingReport)
        # The chat template which is used to grade the resume
        self.chat_template = ChatPromptTemplate.from_messages(
            [
                SystemMessagePromptTemplate.from_template(
                    "You are a hiring manager looking at resumes which are best suited for a job. \
                    The resume and job will be sent as a JSON object where the resume will be in the 'resume' \
                    key and the job will be in the 'job' key. You are to give the score of the resume out of 100, the pros, the cons and the ways to improve it"
                ),
                HumanMessagePromptTemplate.from_template("{input}"),
            ]
        )
    
    async def grade_resume(self, resume: dict) -> ResumeGradingReport:
        # Convert the resume to a json string
        resume_json = json.dumps(resume)
        # Get the messages from the chat template
        messages = self.chat_template.format_prompt(input=resume_json).to_messages()
        # Invoke the model with the messages
        return await self.model.ainvoke(messages)
    
if __name__ == "__main__":
    from llm import model
    # The resume to be graded
    with open("test_data/test_resume.json", "r") as f:
        print("Grading the resume")
        test_resume = f.read()
    
        # Load the json data and clean it
        print("Cleaning the data")
        json_data = json.loads(test_resume)
        cleaned_data = clean_resume(json_data)
        test_resume = json.dumps(cleaned_data)
        
        # The GeminiResumeGraderService which is used to grade the resume
        # The report of the resume
        print("Activating the LLM model")
        resume_grader = LangChainResumeGraderService(model)
        print("Grading the resume")
        report = resume_grader.grade_resume(test_resume)
        
        # Print the report
        print("Score", report.score)
        print("Pros", report.pros)
        print("Cons", report.cons)
        print("Improvements", report.improvements)