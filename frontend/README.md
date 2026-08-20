# 🎨 LocalJobs — Frontend Application

Modern, responsive Single Page Application (SPA) built with pure HTML5, Vanilla CSS3, and modern JavaScript (ES6+).

---

## 📁 Directory Structure
```
frontend/
├── index.html       # Main single page layout and modals
├── css/
│   └── style.css    # Modern Indigo-Teal design system & responsive rules
├── js/
│   ├── api.js       # REST client with dynamic API_BASE detection
│   ├── state.js     # Central reactive state & persona switcher
│   └── app.js       # UI rendering, geolocation filtering, lifecycle actions
├── vercel.json      # 1-Click Vercel deployment configuration
└── package.json     # Optional local serve scripts
```

---

## 🚀 How to Run Locally

### Option 1: Direct in Browser
Double-click `frontend/index.html` or open via `file://...` in any browser.

### Option 2: Using Live Server / Node Serve
```bash
cd frontend
npx serve -l 3000 .
```
Then visit [http://localhost:3000](http://localhost:3000). The frontend automatically connects to the Spring Boot backend running at `http://localhost:8080`.

---

## ☁️ Vercel Deployment
1. Push to GitHub.
2. In Vercel, import the repository and set **Root Directory** to `frontend`.
3. Deploy!
