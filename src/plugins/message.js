// Simple message plugin for Vue 3
export default {
  install(app) {
    const message = {
      success(text) {
        this.show(text, 'success')
      },
      error(text) {
        this.show(text, 'error')
      },
      warning(text) {
        this.show(text, 'warning')
      },
      info(text) {
        this.show(text, 'info')
      },
      show(text, type = 'info') {
        // Create message element
        const messageEl = document.createElement('div')
        messageEl.className = `message message-${type}`
        messageEl.textContent = text
        
        // Add styles
        Object.assign(messageEl.style, {
          position: 'fixed',
          top: '20px',
          left: '50%',
          transform: 'translateX(-50%)',
          padding: '12px 24px',
          borderRadius: '4px',
          color: 'white',
          fontSize: '14px',
          zIndex: '9999',
          maxWidth: '400px',
          textAlign: 'center',
          boxShadow: '0 2px 8px rgba(0,0,0,0.2)',
          transition: 'all 0.3s ease'
        })
        
        // Set background color based on type
        const colors = {
          success: '#52c41a',
          error: '#ff4d4f',
          warning: '#faad14',
          info: '#1890ff'
        }
        messageEl.style.backgroundColor = colors[type] || colors.info
        
        // Add to DOM
        document.body.appendChild(messageEl)
        
        // Auto remove after 3 seconds
        setTimeout(() => {
          if (messageEl.parentNode) {
            messageEl.style.opacity = '0'
            messageEl.style.transform = 'translateX(-50%) translateY(-20px)'
            setTimeout(() => {
              if (messageEl.parentNode) {
                document.body.removeChild(messageEl)
              }
            }, 300)
          }
        }, 3000)
      }
    }
    
    // Add to global properties
    app.config.globalProperties.$message = message
    
    // Also provide as a composable
    app.provide('message', message)
  }
}