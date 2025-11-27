# 📡 Template APIs & JSON Reference

## 🎯 Quick Overview

Tài liệu tổng hợp các API endpoints và JSON format để tạo và quản lý CV templates.

---

## 1️⃣ POST /api/templates - Tạo Template Mới

### Request Body
```json
{
  "name": "Professional Modern",
  "description": "Clean and professional layout for developers",
  "category": "Professional",
  "style": "Modern",
  "isPremium": false,
  "thumbnailUrl": "https://res.cloudinary.com/.../thumbnail.png",
  "htmlStructure": "<!DOCTYPE html>\n<html>\n<head>\n    <meta charset=\"UTF-8\">\n    <style>\n        :root {\n            --primary-color: #3498db;\n            --secondary-color: #2c3e50;\n        }\n        body { font-family: 'Open Sans', sans-serif; }\n        .cv-header { text-align: center; }\n    </style>\n</head>\n<body>\n    <div class=\"cv-container\">\n        <header class=\"cv-header\">\n            <h1>{{user.firstName}} {{user.lastName}}</h1>\n            <p>{{user.email}}</p>\n        </header>\n        <section class=\"cv-section\">\n            <h2>Work Experience</h2>\n            {{#each experiences}}\n            <div class=\"experience-item\">\n                <h3>{{title}} - {{company}}</h3>\n                <p>{{duration.start}} - {{duration.end}}</p>\n            </div>\n            {{/each}}\n        </section>\n    </div>\n</body>\n</html>",
  "templateConfig": {
    "layout": {
      "type": "single-column",
      "sections": ["header", "summary", "experience", "education", "skills"]
    },
    "colors": {
      "primary": "#3498db",
      "secondary": "#2c3e50",
      "accent": "#e74c3c",
      "text": "#333333",
      "textLight": "#7f8c8d",
      "background": "#ffffff",
      "border": "#ecf0f1"
    },
    "typography": {
      "headingFont": "Roboto",
      "bodyFont": "Open Sans",
      "headingSize": "24px",
      "bodySize": "14px",
      "lineHeight": "1.6",
      "letterSpacing": "0px"
    },
    "spacing": {
      "sectionGap": "20px",
      "padding": "40px",
      "margin": "10px"
    }
  }
}
```

### Response (201 Created)
```json
{
  "id": 5,
  "name": "Professional Modern",
  "description": "Clean and professional layout for developers",
  "category": "Professional",
  "style": "Modern",
  "thumbnailUrl": "https://res.cloudinary.com/.../thumbnail.png",
  "compiledFilePath": "d:/templates/professional-modern-a1b2c3d4.html",
  "isPremium": false,
  "templateConfig": {
    "layout": {
      "type": "single-column",
      "sections": ["header", "summary", "experience", "education", "skills"]
    },
    "colors": {
      "primary": "#3498db",
      "secondary": "#2c3e50",
      "accent": "#e74c3c",
      "text": "#333333",
      "textLight": "#7f8c8d",
      "background": "#ffffff",
      "border": "#ecf0f1"
    },
    "typography": {
      "headingFont": "Roboto",
      "bodyFont": "Open Sans",
      "headingSize": "24px",
      "bodySize": "14px",
      "lineHeight": "1.6",
      "letterSpacing": "0px"
    },
    "spacing": {
      "sectionGap": "20px",
      "padding": "40px",
      "margin": "10px"
    }
  },
  "createdAt": "2025-11-20T10:30:00",
  "updatedAt": "2025-11-20T10:30:00"
}
```

---

## 2️⃣ POST /api/upload - Upload Thumbnail

### Request
```
Content-Type: multipart/form-data
Authorization: Bearer <token>

file: <binary image data>
```

### Response (200 OK)
```json
{
  "url": "https://res.cloudinary.com/democloud/image/upload/v1234567890/templates/thumbnail-abc123.png",
  "publicId": "templates/thumbnail-abc123",
  "format": "png",
  "width": 800,
  "height": 1000,
  "size": 245678
}
```

---

## 3️⃣ GET /api/templates - Danh Sách Templates

### Query Parameters
```
?page=1
&size=10
&category=Professional
&style=Modern
&isPremium=false
```

### Response (200 OK)
```json
{
  "data": [
    {
      "id": 1,
      "name": "Professional Modern",
      "description": "Clean and professional layout",
      "category": "Professional",
      "style": "Modern",
      "thumbnailUrl": "https://res.cloudinary.com/.../thumb1.png",
      "isPremium": false,
      "createdAt": "2025-11-15T10:00:00",
      "updatedAt": "2025-11-15T10:00:00"
    },
    {
      "id": 2,
      "name": "Creative Colorful",
      "description": "Vibrant and creative design",
      "category": "Creative",
      "style": "Colorful",
      "thumbnailUrl": "https://res.cloudinary.com/.../thumb2.png",
      "isPremium": true,
      "createdAt": "2025-11-16T14:30:00",
      "updatedAt": "2025-11-16T14:30:00"
    }
  ],
  "pagination": {
    "page": 1,
    "size": 10,
    "total": 25,
    "totalPages": 3
  }
}
```

---

## 4️⃣ GET /api/templates/{id} - Chi Tiết Template

### Response (200 OK)
```json
{
  "id": 5,
  "name": "Professional Modern",
  "description": "Clean and professional layout for developers",
  "category": "Professional",
  "style": "Modern",
  "thumbnailUrl": "https://res.cloudinary.com/.../thumbnail.png",
  "compiledFilePath": "d:/templates/professional-modern-a1b2c3d4.html",
  "isPremium": false,
  "templateConfig": {
    "layout": {
      "type": "single-column",
      "sections": ["header", "summary", "experience", "education", "skills"]
    },
    "colors": {
      "primary": "#3498db",
      "secondary": "#2c3e50",
      "accent": "#e74c3c",
      "text": "#333333",
      "textLight": "#7f8c8d",
      "background": "#ffffff",
      "border": "#ecf0f1"
    },
    "typography": {
      "headingFont": "Roboto",
      "bodyFont": "Open Sans",
      "headingSize": "24px",
      "bodySize": "14px",
      "lineHeight": "1.6",
      "letterSpacing": "0px"
    },
    "spacing": {
      "sectionGap": "20px",
      "padding": "40px",
      "margin": "10px"
    }
  },
  "createdAt": "2025-11-20T10:30:00",
  "updatedAt": "2025-11-20T10:30:00"
}
```

---

## 5️⃣ PUT /api/templates/{id} - Cập Nhật Template

### Request Body
```json
{
  "name": "Professional Modern v2",
  "description": "Updated description",
  "category": "Professional",
  "style": "Modern",
  "isPremium": false,
  "thumbnailUrl": "https://res.cloudinary.com/.../new-thumbnail.png",
  "htmlStructure": "<!DOCTYPE html>...",
  "templateConfig": {
    "layout": {
      "type": "two-column",
      "sections": ["header", "summary", "experience", "education", "skills", "projects"]
    },
    "colors": {
      "primary": "#2c3e50",
      "secondary": "#34495e",
      "accent": "#3498db",
      "text": "#2c3e50",
      "textLight": "#95a5a6",
      "background": "#ffffff",
      "border": "#bdc3c7"
    },
    "typography": {
      "headingFont": "Montserrat",
      "bodyFont": "Lato",
      "headingSize": "26px",
      "bodySize": "15px",
      "lineHeight": "1.7",
      "letterSpacing": "0.5px"
    },
    "spacing": {
      "sectionGap": "24px",
      "padding": "48px",
      "margin": "12px"
    }
  }
}
```

### Response (200 OK)
```json
{
  "id": 5,
  "name": "Professional Modern v2",
  "description": "Updated description",
  "category": "Professional",
  "style": "Modern",
  "thumbnailUrl": "https://res.cloudinary.com/.../new-thumbnail.png",
  "compiledFilePath": "d:/templates/professional-modern-v2-x9y8z7w6.html",
  "isPremium": false,
  "templateConfig": {
    "layout": {
      "type": "two-column",
      "sections": ["header", "summary", "experience", "education", "skills", "projects"]
    },
    "colors": {
      "primary": "#2c3e50",
      "secondary": "#34495e",
      "accent": "#3498db",
      "text": "#2c3e50",
      "textLight": "#95a5a6",
      "background": "#ffffff",
      "border": "#bdc3c7"
    },
    "typography": {
      "headingFont": "Montserrat",
      "bodyFont": "Lato",
      "headingSize": "26px",
      "bodySize": "15px",
      "lineHeight": "1.7",
      "letterSpacing": "0.5px"
    },
    "spacing": {
      "sectionGap": "24px",
      "padding": "48px",
      "margin": "12px"
    }
  },
  "createdAt": "2025-11-20T10:30:00",
  "updatedAt": "2025-11-20T15:45:00"
}
```

---

## 6️⃣ DELETE /api/templates/{id} - Xóa Template

### Response (204 No Content)
```
(empty body)
```

---

## 7️⃣ GET /api/templates/{id}/preview - Preview Template

### Query Parameters
```
?userId=123
```

### Response (200 OK - HTML Content)
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <style>
        :root {
            --primary-color: #3498db;
            --secondary-color: #2c3e50;
        }
        body {
            font-family: 'Open Sans', sans-serif;
            color: #333333;
        }
        .cv-header {
            text-align: center;
            border-bottom: 2px solid #3498db;
        }
    </style>
</head>
<body>
    <div class="cv-container">
        <header class="cv-header">
            <h1>John Doe</h1>
            <p>john.doe@example.com</p>
        </header>
        <section class="cv-section">
            <h2>Work Experience</h2>
            <div class="experience-item">
                <h3>Senior Developer - Tech Corp</h3>
                <p>2020 - Present</p>
            </div>
        </section>
    </div>
</body>
</html>
```

---

## 📦 Template Config Object Structure

```json
{
  "layout": {
    "type": "single-column | two-column | three-column",
    "sections": ["header", "summary", "experience", "education", "skills", "projects", "awards", "languages", "certifications"]
  },
  "colors": {
    "primary": "#hex",
    "secondary": "#hex",
    "accent": "#hex",
    "text": "#hex",
    "textLight": "#hex",
    "background": "#hex",
    "border": "#hex"
  },
  "typography": {
    "headingFont": "Font Name",
    "bodyFont": "Font Name",
    "headingSize": "24px",
    "bodySize": "14px",
    "lineHeight": "1.6",
    "letterSpacing": "0px"
  },
  "spacing": {
    "sectionGap": "20px",
    "padding": "40px",
    "margin": "10px"
  }
}
```

---

## 🎨 HTML Placeholder Syntax

### User Info
```html
{{user.firstName}}
{{user.lastName}}
{{user.title}}
{{user.email}}
{{user.phone}}
{{user.location}}
```

### Sections with Loops
```html
<!-- Experience -->
{{#each experiences}}
  <div class="experience-item">
    <h3>{{title}} - {{company}}</h3>
    <p>{{duration.start}} - {{duration.end}}</p>
    <p>{{location.city}}, {{location.country}}</p>
    <ul>
      {{#each description}}
      <li>{{this}}</li>
      {{/each}}
    </ul>
  </div>
{{/each}}

<!-- Education -->
{{#each educations}}
  <div class="education-item">
    <h3>{{qualification}} - {{institution}}</h3>
    <p>{{duration.start}} - {{duration.end}}</p>
  </div>
{{/each}}

<!-- Skills -->
{{#each skills}}
  <div class="skill-item">
    <span>{{name}}</span>
    <span>{{proficiencyLevel}}</span>
  </div>
{{/each}}

<!-- Projects -->
{{#each projects}}
  <div class="project-item">
    <h3>{{title}}</h3>
    <p>{{duration.start}} - {{duration.end}}</p>
    <ul>
      {{#each description}}
      <li>{{this}}</li>
      {{/each}}
    </ul>
  </div>
{{/each}}
```

---

## 🚀 Frontend Usage Example

```javascript
// 1. Generate HTML from drag & drop builder
const htmlStructure = generateHTMLFromSections(sections, config);

// 2. Upload thumbnail
const formData = new FormData();
formData.append('file', thumbnailBlob);

const uploadResponse = await fetch('/api/upload', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`
  },
  body: formData
});

const { url: thumbnailUrl } = await uploadResponse.json();

// 3. Create template
const response = await fetch('/api/templates', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  },
  body: JSON.stringify({
    name: 'My New Template',
    description: 'Description here',
    category: 'Professional',
    style: 'Modern',
    isPremium: false,
    thumbnailUrl: thumbnailUrl,
    htmlStructure: htmlStructure,
    templateConfig: config
  })
});

const template = await response.json();
console.log('Template created:', template.id);
```

---

## ✅ Summary

**APIs cần implement:**
- ✅ `POST /api/upload` - Upload thumbnail
- ✅ `POST /api/templates` - Tạo template mới
- ✅ `GET /api/templates` - Danh sách templates (có phân trang, filter)
- ✅ `GET /api/templates/{id}` - Chi tiết template
- ✅ `PUT /api/templates/{id}` - Cập nhật template
- ✅ `DELETE /api/templates/{id}` - Xóa template
- ✅ `GET /api/templates/{id}/preview` - Preview với sample data

**Key Points:**
- `htmlStructure` chứa HTML với `{{placeholders}}`
- `templateConfig` chứa colors, typography, spacing
- Backend convert `{{placeholders}}` → Thymeleaf syntax
- Backend save file to disk + metadata to DB
