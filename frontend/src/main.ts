import { createApp } from 'vue'
import { createPinia } from 'pinia'
import persistedState from 'pinia-plugin-persistedstate'
import App from './App.vue'
import router from '@/router'
import '@/styles/tailwind.css'

import Toast from 'vue-toastification'
import 'vue-toastification/dist/index.css'

const app = createApp(App)
app.use(createPinia().use(persistedState))
app.use(router)
app.use(Toast, {
  timeout: 3000,
  position: 'bottom-right',
  closeOnClick: true,
  pauseOnHover: true,
})
app.mount('#app')

