<template>
  <div class="franchise-home">
    <div class="hero-section">
      <div class="hero-content">
        <h1>加盟我们的餐厅</h1>
        <p class="hero-subtitle">携手共创美食事业，开启财富人生</p>
        <div class="hero-features">
          <div class="feature-item">
            <i class="icon">🏪</i>
            <span>品牌支持</span>
          </div>
          <div class="feature-item">
            <i class="icon">📈</i>
            <span>运营指导</span>
          </div>
          <div class="feature-item">
            <i class="icon">💰</i>
            <span>盈利保障</span>
          </div>
          <div class="feature-item">
            <i class="icon">🎯</i>
            <span>市场优势</span>
          </div>
        </div>
      </div>
    </div>

    <div class="action-section">
      <div class="action-cards">
        <div class="action-card" @click="goToApplication">
          <div class="card-icon">
            <i class="icon-apply">📝</i>
          </div>
          <h3>申请加盟</h3>
          <p>填写申请表单，开始您的加盟之旅</p>
          <div class="card-button">
            <span>立即申请</span>
            <i class="arrow">→</i>
          </div>
        </div>

        <div class="action-card" @click="goToStatus">
          <div class="card-icon">
            <i class="icon-status">🔍</i>
          </div>
          <h3>查询进度</h3>
          <p>查看您的申请审核状态和进度</p>
          <div class="card-button">
            <span>查询状态</span>
            <i class="arrow">→</i>
          </div>
        </div>
      </div>
    </div>

    <div class="info-section">
      <div class="info-container">
        <h2>加盟优势</h2>
        <div class="advantages-grid">
          <div class="advantage-item">
            <div class="advantage-icon">🎯</div>
            <h4>品牌实力</h4>
            <p>知名餐饮品牌，市场认知度高，客源稳定</p>
          </div>
          <div class="advantage-item">
            <div class="advantage-icon">📚</div>
            <h4>培训支持</h4>
            <p>完善的培训体系，从技术到管理全方位指导</p>
          </div>
          <div class="advantage-item">
            <div class="advantage-icon">🛠️</div>
            <h4>运营支持</h4>
            <p>专业运营团队，提供选址、装修、营销等支持</p>
          </div>
          <div class="advantage-item">
            <div class="advantage-icon">💎</div>
            <h4>产品优势</h4>
            <p>独特配方，标准化制作，确保产品品质</p>
          </div>
          <div class="advantage-item">
            <div class="advantage-icon">📊</div>
            <h4>数据支持</h4>
            <p>智能管理系统，实时数据分析，科学决策</p>
          </div>
          <div class="advantage-item">
            <div class="advantage-icon">🤝</div>
            <h4>持续服务</h4>
            <p>长期合作伙伴关系，持续提供经营指导</p>
          </div>
        </div>
      </div>
    </div>

    <div class="process-section">
      <div class="process-container">
        <h2>加盟流程</h2>
        <div class="process-steps">
          <div class="step-item">
            <div class="step-number">1</div>
            <div class="step-content">
              <h4>提交申请</h4>
              <p>填写加盟申请表，提供基本信息</p>
            </div>
          </div>
          <div class="step-arrow">→</div>
          <div class="step-item">
            <div class="step-number">2</div>
            <div class="step-content">
              <h4>资格审核</h4>
              <p>我们会对您的申请进行全面评估</p>
            </div>
          </div>
          <div class="step-arrow">→</div>
          <div class="step-item">
            <div class="step-number">3</div>
            <div class="step-content">
              <h4>实地考察</h4>
              <p>双方进行实地考察和深入沟通</p>
            </div>
          </div>
          <div class="step-arrow">→</div>
          <div class="step-item">
            <div class="step-number">4</div>
            <div class="step-content">
              <h4>签约开店</h4>
              <p>签署合同，开始筹备开店事宜</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="requirements-section">
      <div class="requirements-container">
        <h2>加盟条件</h2>
        <div class="requirements-grid">
          <div class="requirement-item">
            <div class="requirement-icon">💰</div>
            <div class="requirement-content">
              <h4>资金要求</h4>
              <p>具备一定的资金实力，投资金额20-50万元</p>
            </div>
          </div>
          <div class="requirement-item">
            <div class="requirement-icon">🏢</div>
            <div class="requirement-content">
              <h4>店面要求</h4>
              <p>商业区或人流密集区，面积80-200平方米</p>
            </div>
          </div>
          <div class="requirement-item">
            <div class="requirement-icon">👤</div>
            <div class="requirement-content">
              <h4>个人要求</h4>
              <p>认同品牌理念，有餐饮行业经验优先</p>
            </div>
          </div>
          <div class="requirement-item">
            <div class="requirement-icon">📍</div>
            <div class="requirement-content">
              <h4>区域要求</h4>
              <p>符合品牌发展规划的区域，无同品牌竞争</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="contact-section">
      <div class="contact-container">
        <h2>联系我们</h2>
        <div class="contact-info">
          <div class="contact-item">
            <i class="contact-icon">📞</i>
            <div class="contact-content">
              <h4>加盟热线</h4>
              <p>400-888-8888</p>
            </div>
          </div>
          <div class="contact-item">
            <i class="contact-icon">📧</i>
            <div class="contact-content">
              <h4>邮箱咨询</h4>
              <p>franchise@restaurant.com</p>
            </div>
          </div>
          <div class="contact-item">
            <i class="contact-icon">🕒</i>
            <div class="contact-content">
              <h4>服务时间</h4>
              <p>周一至周日 9:00-18:00</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="cta-section">
      <div class="cta-container">
        <h2>准备好开始了吗？</h2>
        <p>立即申请加盟，开启您的创业之路</p>
        <div class="cta-buttons">
          <button @click="goToApplication" class="btn-primary">立即申请</button>
          <button @click="goToStatus" class="btn-secondary">查询进度</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'FranchiseHome',
  methods: {
    goToApplication() {
      this.$router.push('/franchise-apply')
    },
    goToStatus() {
      this.$router.push('/franchise-status')
    }
  }
}
</script>

<style scoped>
.franchise-home {
  background: #f5f5f5;
  min-height: 100vh;
}

.hero-section {
  background: linear-gradient(135deg, #d4af37 0%, #b8941f 100%);
  color: white;
  padding: 80px 20px;
  text-align: center;
}

.hero-content h1 {
  font-size: 48px;
  font-weight: bold;
  margin-bottom: 20px;
  text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
}

.hero-subtitle {
  font-size: 20px;
  margin-bottom: 40px;
  opacity: 0.9;
}

.hero-features {
  display: flex;
  justify-content: center;
  gap: 40px;
  flex-wrap: wrap;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.feature-item .icon {
  font-size: 32px;
  margin-bottom: 5px;
}

.feature-item span {
  font-size: 16px;
  font-weight: 500;
}

.action-section {
  padding: 60px 20px;
  background: white;
}

.action-cards {
  display: flex;
  justify-content: center;
  gap: 40px;
  max-width: 800px;
  margin: 0 auto;
  flex-wrap: wrap;
}

.action-card {
  background: white;
  border-radius: 20px;
  padding: 40px 30px;
  text-align: center;
  box-shadow: 0 10px 30px rgba(0,0,0,0.1);
  cursor: pointer;
  transition: all 0.3s ease;
  flex: 1;
  min-width: 300px;
  max-width: 350px;
}

.action-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 20px 40px rgba(0,0,0,0.15);
}

.card-icon {
  font-size: 48px;
  margin-bottom: 20px;
}

.action-card h3 {
  color: #333;
  font-size: 24px;
  margin-bottom: 15px;
}

.action-card p {
  color: #666;
  font-size: 16px;
  margin-bottom: 25px;
  line-height: 1.6;
}

.card-button {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #d4af37;
  font-weight: 600;
  font-size: 16px;
}

.card-button .arrow {
  font-size: 18px;
  transition: transform 0.3s ease;
}

.action-card:hover .card-button .arrow {
  transform: translateX(5px);
}

.info-section,
.process-section,
.requirements-section,
.contact-section {
  padding: 80px 20px;
}

.info-section {
  background: #f8f9fa;
}

.process-section {
  background: white;
}

.requirements-section {
  background: #f8f9fa;
}

.contact-section {
  background: white;
}

.info-container,
.process-container,
.requirements-container,
.contact-container {
  max-width: 1200px;
  margin: 0 auto;
}

.info-section h2,
.process-section h2,
.requirements-section h2,
.contact-section h2 {
  text-align: center;
  font-size: 36px;
  color: #333;
  margin-bottom: 50px;
}

.advantages-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 30px;
}

.advantage-item {
  background: white;
  padding: 30px;
  border-radius: 15px;
  text-align: center;
  box-shadow: 0 5px 15px rgba(0,0,0,0.08);
  transition: transform 0.3s ease;
}

.advantage-item:hover {
  transform: translateY(-5px);
}

.advantage-icon {
  font-size: 48px;
  margin-bottom: 20px;
}

.advantage-item h4 {
  color: #333;
  font-size: 20px;
  margin-bottom: 15px;
}

.advantage-item p {
  color: #666;
  line-height: 1.6;
}

.process-steps {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 20px;
}

.step-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  max-width: 200px;
}

.step-number {
  width: 60px;
  height: 60px;
  background: #d4af37;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 20px;
}

.step-content h4 {
  color: #333;
  font-size: 18px;
  margin-bottom: 10px;
}

.step-content p {
  color: #666;
  font-size: 14px;
  line-height: 1.5;
}

.step-arrow {
  font-size: 24px;
  color: #d4af37;
  margin: 0 10px;
}

.requirements-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 30px;
}

.requirement-item {
  background: white;
  padding: 30px;
  border-radius: 15px;
  display: flex;
  align-items: flex-start;
  gap: 20px;
  box-shadow: 0 5px 15px rgba(0,0,0,0.08);
  transition: transform 0.3s ease;
}

.requirement-item:hover {
  transform: translateY(-5px);
}

.requirement-icon {
  font-size: 40px;
  flex-shrink: 0;
}

.requirement-content h4 {
  color: #333;
  font-size: 18px;
  margin-bottom: 10px;
}

.requirement-content p {
  color: #666;
  line-height: 1.6;
}

.contact-info {
  display: flex;
  justify-content: center;
  gap: 60px;
  flex-wrap: wrap;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 20px;
  text-align: left;
}

.contact-icon {
  font-size: 32px;
  color: #d4af37;
}

.contact-content h4 {
  color: #333;
  font-size: 18px;
  margin-bottom: 5px;
}

.contact-content p {
  color: #666;
  font-size: 16px;
}

.cta-section {
  background: linear-gradient(135deg, #333 0%, #555 100%);
  color: white;
  padding: 80px 20px;
  text-align: center;
}

.cta-container h2 {
  font-size: 36px;
  margin-bottom: 20px;
}

.cta-container p {
  font-size: 18px;
  margin-bottom: 40px;
  opacity: 0.9;
}

.cta-buttons {
  display: flex;
  justify-content: center;
  gap: 20px;
  flex-wrap: wrap;
}

.btn-primary,
.btn-secondary {
  padding: 15px 40px;
  border: none;
  border-radius: 50px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 160px;
}

.btn-primary {
  background: #d4af37;
  color: white;
}

.btn-primary:hover {
  background: #b8941f;
  transform: translateY(-2px);
}

.btn-secondary {
  background: transparent;
  color: white;
  border: 2px solid white;
}

.btn-secondary:hover {
  background: white;
  color: #333;
  transform: translateY(-2px);
}

@media (max-width: 768px) {
  .franchise-home {
    padding: 0;
  }
  
  .hero-content h1 {
    font-size: 32px;
  }
  
  .hero-subtitle {
    font-size: 16px;
  }
  
  .hero-features {
    gap: 20px;
  }
  
  .action-cards {
    flex-direction: column;
    align-items: center;
  }
  
  .action-card {
    min-width: auto;
    max-width: 400px;
    width: 100%;
  }
  
  .advantages-grid {
    grid-template-columns: 1fr;
  }
  
  .process-steps {
    flex-direction: column;
  }
  
  .step-arrow {
    transform: rotate(90deg);
    margin: 10px 0;
  }
  
  .requirements-grid {
    grid-template-columns: 1fr;
  }
  
  .contact-info {
    flex-direction: column;
    gap: 30px;
  }
  
  .contact-item {
    justify-content: center;
  }
  
  .cta-buttons {
    flex-direction: column;
    align-items: center;
  }
  
  .btn-primary,
  .btn-secondary {
    width: 100%;
    max-width: 300px;
  }
}
</style>