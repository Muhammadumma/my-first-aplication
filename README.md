# JSP Dutse Clearance

A modern React + TypeScript student clearance portal for Jigawa State Polytechnic Dutse. The application helps students complete clearance tasks, upload required documents, review alerts, track status, and interact with an AI assistant for guidance.

## Overview

This project is built as a web application for managing the student clearance process. It includes:

- Student authentication and profile flow
- Dashboard overview for clearance status
- Task-based clearance workflow
- Document upload and verification flow
- QR-based scanning support
- Alerts and notifications
- AI-powered assistant for student help
- Responsive desktop/mobile interface

## Tech Stack

- React 19
- TypeScript
- Vite
- Tailwind CSS
- Firebase Authentication and Firestore
- Gemini AI integration
- Lucide icons
- QR code support

## Features

### Student portal
- Secure sign-in and registration flow
- Student profile management
- Dashboard summarizing clearance progress
- Task tracking and updates

### Document management
- Upload required student documents
- Multi-step document workflow
- Camera capture support for images
- Certificate modal previews

### Navigation and UX
- Responsive sidebar navigation
- Mobile-friendly bottom navigation
- Animated UI interactions and polished layouts

### AI assistant
- Built-in AI chat support to help students navigate tasks and understand clearance requirements

## Project Structure

```text
my-first-aplication/
├── src/
│   ├── App.tsx
│   ├── firebase.ts
│   ├── main.tsx
│   ├── index.css
│   ├── components/
│   │   ├── BottomNav.tsx
│   │   ├── CameraCaptureModal.tsx
│   │   ├── CertificateModal.tsx
│   │   ├── JigawaPolyLogo.tsx
│   │   ├── QrScannerModal.tsx
│   │   ├── SidebarNav.tsx
│   │   └── TopHeader.tsx
│   ├── context/
│   │   └── ClearanceContext.tsx
│   ├── screens/
│   │   ├── AiAssistantScreen.tsx
│   │   ├── AlertsScreen.tsx
│   │   ├── AuthScreen.tsx
│   │   ├── DocumentUploadScreen.tsx
│   │   ├── HomeScreen.tsx
│   │   ├── ProfileScreen.tsx
│   │   └── TasksScreen.tsx
│   ├── services/
│   │   └── geminiService.ts
│   ├── data/
│   ├── types/
│   └── ...
├── public/
│   └── assets/
├── .env.example
├── .gitignore
├── index.html
├── metadata.json
├── package.json
├── package-lock.json
├── tsconfig.json
├── vite.config.ts
└── README.md
```

## Getting Started

### Prerequisites

- Node.js 18+
- npm
- Firebase project credentials
- Gemini API configuration (if using AI assistant features)

### Installation

1. Clone the repository

```bash
git clone https://github.com/Muhammadumma/my-first-aplication.git
cd my-first-aplication
```

2. Install dependencies

```bash
npm install
```

3. Configure environment variables

Copy the sample environment file and add your Firebase configuration:

```bash
cp .env.example .env.local
```

Then update `.env.local` with your project values.

### Run locally

```bash
npm run dev
```

The app runs by default on:

```text
http://localhost:3000
```

### Production build

```bash
npm run build
```

### Lint / Type check

```bash
npm run lint
```

## Environment Configuration

This project expects Firebase environment variables such as:

```env
VITE_FIREBASE_API_KEY=
VITE_FIREBASE_AUTH_DOMAIN=
VITE_FIREBASE_PROJECT_ID=
VITE_FIREBASE_STORAGE_BUCKET=
VITE_FIREBASE_MESSAGING_SENDER_ID=
VITE_FIREBASE_APP_ID=
VITE_FIREBASE_MEASUREMENT_ID=
```

Example values are included in `.env.example`.

## Important Notes

- The app is configured to run on port `3000`.
- The project uses `localhost` and `0.0.0.0` bindings in the Vite config for local accessibility.
- Firebase is initialized in `src/firebase.ts` and used across the app for authentication and database access.

## License

This project is currently unlicensed unless otherwise stated by the repository owner.

## Contributing

Contributions are welcome. If you want to improve the app, feel free to fork the repo and submit a pull request.

## Support

For questions or project support, contact the repository owner or review the project configuration files in the repo.
