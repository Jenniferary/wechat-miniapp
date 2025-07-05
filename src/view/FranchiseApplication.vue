<template>
  <div class="franchise-application">
    <div class="header">
      <h1>加盟申请</h1>
      <p>欢迎加入我们的餐厅连锁大家庭</p>
    </div>

    <div class="form-container">
      <form @submit.prevent="submitApplication">
        <div class="form-section">
          <h3>申请人信息</h3>
          <div class="form-group">
            <label>姓名 *</label>
            <input v-model="form.applicantName" type="text" required placeholder="请输入您的姓名">
          </div>
          <div class="form-group">
            <label>手机号 *</label>
            <input v-model="form.applicantPhone" type="tel" required placeholder="请输入手机号">
          </div>
          <div class="form-group">
            <label>身份证号 *</label>
            <input v-model="form.applicantIdCard" type="text" required placeholder="请输入身份证号">
          </div>
          <div class="form-group">
            <label>邮箱</label>
            <input v-model="form.applicantEmail" type="email" placeholder="请输入邮箱地址">
          </div>
        </div>

        <div class="form-section">
          <h3>店铺信息</h3>
          <div class="form-group">
            <label>拟开店名称 *</label>
            <input v-model="form.proposedStoreName" type="text" required placeholder="请输入拟开店名称">
          </div>
          <div class="form-group">
            <label>拟开店地址 *</label>
            <input v-model="form.proposedLocation" type="text" required placeholder="请输入详细地址">
          </div>
          <div class="form-group">
            <label>投资金额 (万元) *</label>
            <input v-model.number="form.investmentAmount" type="number" required placeholder="请输入投资金额" min="0" step="0.1">
          </div>
          <div class="form-group">
            <label>预计开业时间</label>
            <input v-model="form.expectedOpeningDate" type="date">
          </div>
        </div>

        <div class="form-section">
          <h3>经营经验</h3>
          <div class="form-group">
            <label>餐饮经验描述</label>
            <textarea v-model="form.businessExperience" placeholder="请简述您的餐饮经验" rows="4"></textarea>
          </div>
        </div>

        <div class="form-section">
          <h3>其他信息</h3>
          <div class="form-group">
            <label>申请理由</label>
            <textarea v-model="form.applicationReason" placeholder="请说明您的申请理由" rows="4"></textarea>
          </div>
        </div>

        <div class="form-actions">
          <button type="button" @click="goBack" class="btn-secondary">返回</button>
          <button type="submit" class="btn-primary" :disabled="submitting">{{ submitting ? '提交中...' : '提交申请' }}</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import { franchiseApplicationAPI, franchiseUtils } from '@/api/franchise'

export default {
  name: 'FranchiseApplication',

  data() {
    return {
      submitting: false,
      form: {
        applicantName: '',
        applicantPhone: '',
        applicantIdCard: '',
        applicantEmail: '',
        proposedStoreName: '',
        proposedLocation: '',
        investmentAmount: null,
        expectedOpeningDate: '',
        businessExperience: '',
        applicationReason: ''
      }
    }
  },
  methods: {
    // 本地验证函数作为备用
    validatePhoneLocal(phone) {
      return /^1[3-9]\d{9}$/.test(phone)
    },
    validateIdCardLocal(idCard) {
      return /^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/.test(idCard)
    },
    async submitApplication() {
      if (!this.validateForm()) {
        return
      }

      this.submitting = true
      try {
        const response = await franchiseApplicationAPI.submitApplication(this.form)
        if (response.data.success) {
          this.$message.success('申请提交成功！我们会尽快审核您的申请。')
          this.$router.push('/franchise-status')
        } else {
          this.$message.error(response.data.message || '提交失败，请重试')
        }
      } catch (error) {
        console.error('提交申请失败:', error)
        this.$message.error('提交失败，请检查网络连接后重试')
      } finally {
        this.submitting = false
      }
    },
    validateForm() {
      if (!this.form.applicantName.trim()) {
        this.$message.error('请输入姓名')
        return false
      }
      if (!this.form.applicantPhone.trim()) {
        this.$message.error('请输入手机号')
        return false
      }
      if (!((franchiseUtils && franchiseUtils.validatePhone(this.form.applicantPhone)) || this.validatePhoneLocal(this.form.applicantPhone))) {
        this.$message.error('请输入正确的手机号')
        return false
      }
      if (!this.form.applicantIdCard.trim()) {
        this.$message.error('请输入身份证号')
        return false
      }
      if (!((franchiseUtils && franchiseUtils.validateIdCard(this.form.applicantIdCard)) || this.validateIdCardLocal(this.form.applicantIdCard))) {
        this.$message.error('请输入正确的身份证号')
        return false
      }
      if (!this.form.proposedStoreName.trim()) {
        this.$message.error('请输入拟开店名称')
        return false
      }
      if (!this.form.proposedLocation.trim()) {
        this.$message.error('请输入拟开店地址')
        return false
      }
      if (!this.form.investmentAmount || this.form.investmentAmount <= 0) {
        this.$message.error('请输入有效的投资金额')
        return false
      }
      return true
    },
    goBack() {
      this.$router.go(-1)
    }
  }
}
</script>

<style scoped>
.franchise-application {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  background: #f5f5f5;
  min-height: 100vh;
}

.header {
  text-align: center;
  margin-bottom: 30px;
  background: white;
  padding: 30px;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.header h1 {
  color: #d4af37;
  margin-bottom: 10px;
  font-size: 28px;
}

.header p {
  color: #666;
  font-size: 16px;
}

.form-container {
  background: white;
  padding: 30px;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.form-section {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.form-section:last-of-type {
  border-bottom: none;
}

.form-section h3 {
  color: #333;
  margin-bottom: 20px;
  font-size: 18px;
  border-left: 4px solid #d4af37;
  padding-left: 10px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #333;
  font-weight: 500;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.3s;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #d4af37;
  box-shadow: 0 0 0 2px rgba(212, 175, 55, 0.2);
}

.form-group textarea {
  resize: vertical;
  min-height: 80px;
}

.form-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.btn-primary,
.btn-secondary {
  padding: 12px 30px;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
  min-width: 120px;
}

.btn-primary {
  background: #d4af37;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #b8941f;
  transform: translateY(-2px);
}

.btn-primary:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background: #545b62;
  transform: translateY(-2px);
}

@media (max-width: 768px) {
  .franchise-application {
    padding: 10px;
  }
  
  .form-container {
    padding: 20px;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .btn-primary,
  .btn-secondary {
    width: 100%;
  }
}
</style>