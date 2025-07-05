

// vue.config.js
module.exports = {
  devServer: {
    port: 8082,  // 🆕 添加这行，指定前端端口
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // 后端地址保持不变
        changeOrigin: true,
        // pathRewrite: { '^/api': '' }
      }
    }
  }
}
