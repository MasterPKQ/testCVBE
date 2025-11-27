package com.example.identity.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class TemplateParserService {

    /**
     * Convert HTML từ Admin Template Builder sang Thymeleaf template
     * Converts placeholders like {{user.name}} to ${cvData.user.name}
     * Handles lists with {{#each items}} to th:each="item : ${items}"
     */
    public String parseHtmlToThymeleaf(String htmlContent) {
        log.info("Starting HTML to Thymeleaf conversion");
        
        // Step 1: Parse HTML with Jsoup
        Document doc = Jsoup.parse(htmlContent);
        doc.outputSettings().prettyPrint(true);
        
        // Step 2: Add Thymeleaf namespace to html tag
        Element htmlTag = doc.selectFirst("html");
        if (htmlTag != null) {
            htmlTag.attr("xmlns:th", "http://www.thymeleaf.org");
        }
        
        // Step 3: Convert placeholders to Thymeleaf expressions
        String html = doc.html();
        
        // Replace simple placeholders: {{user.name}} -> ${cvData.user.name}
        html = convertSimplePlaceholders(html);
        
        // Replace loop constructs: {{#each items}} ... {{/each}}
        html = convertLoopPlaceholders(html);
        
        // Replace conditional constructs: {{#if condition}} ... {{/if}}
        html = convertConditionalPlaceholders(html);
        
        log.info("HTML to Thymeleaf conversion completed");
        return html;
    }

    /**
     * Convert {{variable}} to ${cvData.variable}
     * Convert {{section.field}} to ${cvData.section.field}
     */
    private String convertSimplePlaceholders(String html) {
        // Pattern để tìm {{something}} nhưng không phải {{#each}} hoặc {{/each}}
        Pattern pattern = Pattern.compile("\\{\\{(?!#|/)([^}]+)\\}\\}");
        Matcher matcher = pattern.matcher(html);
        
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String placeholder = matcher.group(1).trim();
            
            // Check if it's a cvData field
            String replacement;
            if (placeholder.startsWith("user.") || placeholder.startsWith("sections.") || 
                placeholder.startsWith("customization.")) {
                replacement = "\\${cvData." + placeholder + "}";
            } else {
                replacement = "\\${cvData." + placeholder + "}";
            }
            
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);
        
        return result.toString();
    }

    /**
     * Convert {{#each items}} ... {{/each}} to th:each="item : ${cvData.items}"
     */
    private String convertLoopPlaceholders(String html) {
        // Pattern: {{#each arrayName}} content {{/each}}
        Pattern pattern = Pattern.compile(
            "<!--\\s*\\{\\{#each\\s+(\\w+)\\}\\}\\s*-->(.*?)<!--\\s*\\{\\{/each\\}\\}\\s*-->",
            Pattern.DOTALL
        );
        
        Matcher matcher = pattern.matcher(html);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String arrayName = matcher.group(1); // e.g., "experiences"
            String content = matcher.group(2);
            
            // Extract the singular form (experiences -> experience)
            String itemName = arrayName.endsWith("s") ? 
                arrayName.substring(0, arrayName.length() - 1) : arrayName + "Item";
            
            // Wrap in a div with th:each
            String replacement = String.format(
                "<div th:each=\"%s : \\${cvData.sections.%s}\">%s</div>",
                itemName, arrayName, content
            );
            
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }

    /**
     * Convert {{#if condition}} ... {{/if}} to th:if="${condition}"
     */
    private String convertConditionalPlaceholders(String html) {
        // Pattern: {{#if condition}} content {{/if}}
        Pattern pattern = Pattern.compile(
            "<!--\\s*\\{\\{#if\\s+([^}]+)\\}\\}\\s*-->(.*?)<!--\\s*\\{\\{/if\\}\\}\\s*-->",
            Pattern.DOTALL
        );
        
        Matcher matcher = pattern.matcher(html);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String condition = matcher.group(1).trim();
            String content = matcher.group(2);
            
            // Wrap in a div with th:if
            String replacement = String.format(
                "<div th:if=\"\\${cvData.%s}\">%s</div>",
                condition, content
            );
            
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }

    /**
     * Extract section definitions from HTML comments or data attributes
     * Example: <!-- SECTION: experiences, education, skills -->
     */
    public String extractSectionsDefinition(String htmlContent) {
        Document doc = Jsoup.parse(htmlContent);
        
        // Look for comment with section definition
        Elements comments = doc.getAllElements();
        for (Element element : comments) {
            if (element.hasAttr("data-sections")) {
                return element.attr("data-sections");
            }
        }
        
        // Default sections
        return "[\"header\", \"summary\", \"experiences\", \"education\", \"skills\", \"projects\"]";
    }

    /**
     * Clean and validate HTML before processing
     */
    public String cleanHtml(String html) {
        Document doc = Jsoup.parse(html);
        
        // Remove script tags for security
        doc.select("script").remove();
        
        // Remove inline event handlers
        Elements allElements = doc.getAllElements();
        for (Element element : allElements) {
            element.removeAttr("onclick");
            element.removeAttr("onload");
            element.removeAttr("onerror");
        }
        
        return doc.html();
    }
}
