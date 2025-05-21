<template>
  <div class="chat-page">
    <h1 class="page-title">AI医疗咨询助手</h1>
    <p class="page-description">
      欢迎使用AI医疗咨询助手，您可以在这里询问健康相关问题，获取疾病信息和专业建议，我们的AI助手将为您提供参考性的医疗咨询。
    </p>
    
    <div class="chat-container">
      <AIChatWindow />
    </div>
    
    <h2 class="section-title">功能特点</h2>
    <div class="feature-grid">
      <div class="feature-card">
        <div class="feature-icon"><i class="el-icon-chat-dot-round"></i></div>
        <h3 class="feature-title">智能问诊</h3>
        <p class="feature-description">通过自然语言交流，描述您的症状，AI助手会进行初步分析并给出建议。</p>
      </div>
      <div class="feature-card">
        <div class="feature-icon"><i class="el-icon-first-aid-kit"></i></div>
        <h3 class="feature-title">科室推荐</h3>
        <p class="feature-description">根据您描述的症状，智能推荐适合的就诊科室，帮助您快速找到合适的医疗资源。</p>
      </div>
      <div class="feature-card">
        <div class="feature-icon"><i class="el-icon-user"></i></div>
        <h3 class="feature-title">医生推荐</h3>
        <p class="feature-description">AI助手可以根据您的症状和需求，推荐专业对口的医生，并提供相关背景信息。</p>
      </div>
      <div class="feature-card">
        <div class="feature-icon"><i class="el-icon-document"></i></div>
        <h3 class="feature-title">健康知识</h3>
        <p class="feature-description">提供常见疾病的相关知识，包括症状、预防、治疗方法等信息。</p>
      </div>
    </div>
    
    <h2 class="section-title">使用指南</h2>
    <div class="feature-grid">
      <div class="feature-card">
        <h3 class="feature-title">如何描述症状</h3>
        <p class="feature-description">尽可能详细地描述您的症状，包括持续时间、严重程度、是否有诱因等，这有助于AI更准确地分析。</p>
      </div>
      <div class="feature-card">
        <h3 class="feature-title">获取科室推荐</h3>
        <p class="feature-description">您可以直接询问"我应该去哪个科室就诊"。</p>
      </div>
      <div class="feature-card">
        <h3 class="feature-title">寻找专业医生</h3>
        <p class="feature-description">描述完症状后，可以询问"有哪些推荐的医生"。</p>
      </div>
      <div class="feature-card">
        <h3 class="feature-title">获取健康知识</h3>
        <p class="feature-description">有什么不开心的都可以跟我说欧~</p>
      </div>
    </div>
    
    <div class="disclaimer">
      免责声明：本AI助手提供的建议仅供参考，不构成医疗诊断或治疗方案。如有严重健康问题，请立即就医或联系医疗专业人士。AI分析基于您提供的信息，可能存在局限性，最终诊断和治疗方案应由专业医生决定。
    </div>
  </div>
</template>

<script>
import AIChatWindow from '@/components/AIChatWindow.vue';

export default {
  name: 'AIChat',
  components: {
    AIChatWindow
  }
}
</script>

<style scoped>
.chat-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem 1.5rem;
}

.page-title {
  font-size: 2.2rem;
  color: #2c3e50;
  text-align: center;
  margin-bottom: 0.5rem;
  font-weight: 600;
}

.page-description {
  text-align: center;
  color: #606266;
  margin-bottom: 2rem;
  font-size: 1.1rem;
  line-height: 1.6;
}

.chat-container {
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
  margin-bottom: 2.5rem;
  padding: 1rem;
  border: 1px solid #ebeef5;
  transition: all 0.3s ease;
}

.section-title {
  font-size: 1.5rem;
  margin: 1.5rem 0 1rem;
  padding-left: 1rem;
  border-left: 4px solid #409EFF;
  background: linear-gradient(to right, rgba(64, 158, 255, 0.1), rgba(64, 158, 255, 0));
  padding: 0.5rem 1rem;
  border-radius: 4px;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.feature-card {
  background-color: #fff;
  border-radius: 8px;
  padding: 1.2rem;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  transition: transform 0.3s, box-shadow 0.3s;
  border: 1px solid #ebeef5;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.feature-icon {
  font-size: 2rem;
  color: #409EFF;
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.feature-title {
  font-size: 1.2rem;
  color: #303133;
  margin-bottom: 0.5rem;
}

.feature-description {
  color: #606266;
  font-size: 0.95rem;
  line-height: 1.5;
}

.disclaimer {
  margin-top: 2rem;
  padding: 1rem;
  background-color: #f8f9fa;
  border-left: 4px solid #e6a23c;
  color: #666;
  font-size: 0.9rem;
  line-height: 1.6;
  border-radius: 4px;
}

@media (max-width: 768px) {
  .feature-grid {
    grid-template-columns: 1fr;
  }
  
  .chat-page {
    padding: 1rem;
  }
  
  .page-title {
    font-size: 1.8rem;
  }
}
</style> 