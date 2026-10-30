import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // Forward API calls to Margo so the browser doesn't hit CORS.
    proxy: { '/assets': 'http://localhost:8081' },
  },
})
