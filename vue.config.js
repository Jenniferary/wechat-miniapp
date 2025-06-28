// vue.config.js

module.exports = {
  devServer: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // 你的 Spring Boot 后端地址
        changeOrigin: true,              // 支持跨域
        // 如果后端接口没有以 /api 开头，则打开下面这行：
        // pathRewrite: { '^/api': '' }
      }
    }
  }
}
