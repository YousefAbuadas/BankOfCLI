# Bank of CLI frontend

React and TypeScript application built with Vite. Source code is organized by
responsibility so application composition, presentation, state, and backend
communication are easy to find.

## Development

```sh
npm install
npm run dev
```

The backend defaults to `http://localhost:8080`. Set `VITE_API_BASE_URL` to use
another backend URL. Authentication and banking endpoints are called through
`src/services/`.

```sh
npm run build
npm run lint
npm run preview
```

## Source structure

```text
src/
├── main.tsx                 # React entry point and global style imports
├── app/                     # App composition and navigation configuration
├── layouts/                 # Sidebar, header, and page-content structure
├── pages/                   # Complete screens and their local interactions
│   ├── auth/
│   ├── overview/
│   ├── transactions/
│   └── activity/
├── components/              # Reusable UI, with styles beside each component
│   ├── ui/                  # Buttons, fields, icons, notifications, empty states
│   ├── banking/             # Transaction list and dashboard loading skeleton
│   └── branding/            # Bank branding
├── hooks/                   # Session, dashboard, and toast state/effect logic
├── services/                # HTTP client, auth operations, and banking adapters
├── types/                   # Domain entities and frontend service contracts
├── utils/                   # Stateless formatting, money, and tab helpers
├── styles/                  # Design tokens, base styles, and shared utilities
└── mocks/                   # Demo store and JSON fixtures
```

## Layer boundaries

- **App** connects pages, layout, and shared hooks. It owns the current view and
  selected account used for navigation.
- **Layouts** arrange the surrounding UI using props and callbacks.
- **Pages** assemble screens and own local state such as filters and form fields.
  Page-local form submissions may call services directly.
- **Components** render reusable UI through props and callbacks. Put generic UI
  in `ui/` and components that understand banking entities in `banking/`.
- **Hooks** encapsulate meaningful React state and effects. `useAuth` owns the
  signed-in user, `useDashboard` handles loading/retry/account updates, and
  `useToast` handles notification state and timeout cleanup.
- **Services** call the backend, map backend responses to frontend models, and
  persist session data. Backend-specific response types stay beside their
  adapters. Services do not import React or UI layers.
- **Types** separate entities in `domain.ts` from auth/banking contracts and
  shared API errors. Keep component-specific props beside their component.
- **Utils** contain stateless functions. Services, types, and utilities never
  import pages, layouts, or components.
- **Mocks** contain demo behavior and fixtures, separate from production service
  code. They are currently not connected to the application's runtime.

## Conventions

Use `PascalCase.tsx` for components, `useSomething.ts` for hooks, and descriptive
`camelCase.ts` names for services and utilities. Import modules directly from
their owning files.

Keep page/component CSS beside the TSX file that imports it. Shared color tokens
belong in `styles/tokens.css`; base styles and utilities used across screens
belong in `styles/globals.css`. Inputs and selects share `components/ui/Field.css`.
Create additional folders when they contain actual code.
