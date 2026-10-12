import { configureApiClient } from '../api/apiClient'
import { adminQueryClient } from '../api/queryClient'
import { AdminAuthSession } from './AdminAuthSession'
import { createAuthService } from './authService'

export const adminAuthSession = new AdminAuthSession(createAuthService(), () => {
  void adminQueryClient.cancelQueries()
  adminQueryClient.clear()
})

configureApiClient(adminAuthSession, { onForbidden: adminAuthSession.denyPermission })
