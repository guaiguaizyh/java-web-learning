<template>
  <div class="ai-chat-container">
    <div class="chat-header">
      <h3>AI 医疗助手</h3>
      <p class="subheader">描述您的症状，我会为您提供医疗建议和科室推荐</p>
    </div>
    
    <div class="messages-container" ref="messagesContainer">
      <div v-for="(message, index) in messages" :key="index" 
          :class="['message', message.type === 'user' ? 'user-message' : 'ai-message']">
        <div class="message-avatar">
          <el-avatar :size="36" :icon="message.type === 'user' ? UserFilled : Service" 
            :class="message.type === 'user' ? 'user-avatar' : 'ai-avatar'"></el-avatar>
        </div>
        <div class="message-content">
          <div class="message-header">
            <span class="sender-name">{{ message.type === 'user' ? '您' : 'AI助手' }}</span>
            <span class="message-time">{{ formatTime(message.timestamp) }}</span>
          </div>
          <div class="message-text" v-html="formatMessage(message.content)"></div>
        </div>
      </div>
      <div v-if="loading" class="loading-indicator">
        <div class="typing-indicator">
          <span></span>
          <span></span>
          <span></span>
        </div>
      </div>
    </div>
    
    <div class="chat-input">
      <el-input
        v-model="inputMessage"
        type="textarea"
        :rows="3"
        :autosize="{ minRows: 2, maxRows: 4 }"
        placeholder="请输入您的问题..."
        class="custom-input"
        @keyup.enter.exact="sendMessage"
        @keyup.enter.shift.prevent="inputMessage += '\n'"
      >
        <template #append>
          <el-button type="primary" class="send-button" @click="sendMessage">
            发送
          </el-button>
        </template>
      </el-input>
      <div class="input-tip">按 Enter 发送，Shift + Enter 换行</div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch, nextTick } from 'vue';
import { 
  sendChatMessage, 
  getDepartmentRecommendation, 
  getDoctorRecommendations,
  getAIDirectRecommendations
} from '../api/ai';
import { UserFilled, Service, Position, House, User, Cpu } from '@element-plus/icons-vue';

export default {
  name: 'AIChatWindow',
  
  setup() {
    const inputMessage = ref('');
    const messages = ref([]);
    const loading = ref(false);
    const messagesContainer = ref(null);
    const lastContext = ref('');
    
    // Check if there's at least one user message
    const hasUserMessage = computed(() => {
      return messages.value.some(message => message.type === 'user');
    });
    
    // Get the most recent user message for recommendations
    const getLastUserMessage = () => {
      for (let i = messages.value.length - 1; i >= 0; i--) {
        if (messages.value[i].type === 'user') {
          return messages.value[i].content;
        }
      }
      return '';
    };
    
    // Format timestamp
    const formatTime = (timestamp) => {
      if (!timestamp) return '';
      const date = new Date(timestamp);
      return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    };
    
    // Format message with line breaks
    const formatMessage = (content) => {
      if (!content) return '';
      return content.replace(/\n/g, '<br>');
    };
    
    // 格式化医生专长显示
    const formatExpertiseDisplay = (doctor) => {
      // 优先使用 expertiseList 数组
      if (doctor.expertiseList && Array.isArray(doctor.expertiseList) && doctor.expertiseList.length > 0) {
        return doctor.expertiseList.map(exp => exp.name || exp.expertiseName || exp).join(', ');
      }
      
      // 如果 expertiseList 是字符串，直接返回
      if (doctor.expertiseList && typeof doctor.expertiseList === 'string' && doctor.expertiseList.trim() !== '') {
        return doctor.expertiseList;
      }
      
      // 其次使用 expertiseListStr 字符串
      if (doctor.expertiseListStr && doctor.expertiseListStr.trim() !== '') {
        return doctor.expertiseListStr;
      }
      
      // 再次使用 specialty 字符串
      if (doctor.specialty && doctor.specialty.trim() !== '') {
        return doctor.specialty;
      }
      
      // 最后兜底显示
      return '暂无专长信息';
    };
    
    // Send message
    const sendMessage = async () => {
      if (!inputMessage.value.trim() || loading.value) return;
      
      // Add user message
      const userMessage = {
        type: 'user',
        content: inputMessage.value.trim(),
        timestamp: new Date().toISOString()
      };
      messages.value.push(userMessage);
      inputMessage.value = '';
      
      // Show loading indicator
      loading.value = true;
      
      try {
        console.log('发送消息，当前上下文:', lastContext.value);
        // Get AI response - 传递 lastContext
        const response = await sendChatMessage(userMessage.content, lastContext.value);
        
        if (response && response.data) {
          let newMessage = null;
          
          // 保存返回的上下文，用于下次对话
          if (response.data.context) {
            lastContext.value = response.data.context;
            console.log('更新上下文:', lastContext.value);
          }
          
          // 处理推荐科室的响应
          if (response.data.context && response.data.context.startsWith('DEPT_REC')) {
            newMessage = {
              type: response.data.message.type || 'assistant',
              content: response.data.message.content,
              timestamp: response.data.message.timestamp || new Date().toISOString()
            };
          } 
          // 处理普通对话响应
          else if (response.data.message) {
            newMessage = {
              type: 'assistant',
              content: response.data.message.content,
              timestamp: new Date().toISOString()
            };
          }
          // 处理直接返回内容的情况
          else if (response.data.content) {
            newMessage = {
              type: 'assistant',
              content: response.data.content,
              timestamp: new Date().toISOString()
            };
          }

          // 检查是否与最后一条消息重复
          if (newMessage) {
            const lastMessage = messages.value[messages.value.length - 1];
            if (!lastMessage || lastMessage.content !== newMessage.content) {
              messages.value.push(newMessage);
            }
          }
        }
      } catch (error) {
        // Handle error
        messages.value.push({
          type: 'assistant',
          content: '抱歉，发生了错误，请稍后再试。',
          timestamp: new Date().toISOString()
        });
      } finally {
        loading.value = false;
        // Scroll to bottom
        scrollToBottom();
      }
    };
    
    // Get department recommendation
    const getDepartmentRecommendation = async () => {
      if (loading.value || !hasUserMessage.value) return;
      
      loading.value = true;
      
      try {
        const symptoms = getLastUserMessage();
        const response = await getDepartmentRecommendation(symptoms);
        
        if (response && response.data) {
          messages.value.push({
            type: response.data.type || 'assistant',
            content: response.data.content,
            timestamp: response.data.timestamp || new Date().toISOString()
          });
        }
      } catch (error) {
        messages.value.push({
          type: 'assistant',
          content: '抱歉，获取科室推荐失败，请稍后再试。',
          timestamp: new Date().toISOString()
        });
      } finally {
        loading.value = false;
        scrollToBottom();
      }
    };
    
    // Get doctor recommendation
    const getDoctorRecommendation = async () => {
      if (loading.value || !hasUserMessage.value) return;
      
      loading.value = true;
      
      try {
        const symptoms = getLastUserMessage();
        const response = await getDoctorRecommendations(symptoms, 3);
        
        if (response && response.data) {
          // Format the doctor recommendation as a nicely formatted message
          let content = `<strong>推荐科室：${response.data.recommendedDepartment}</strong><br>`;
          content += `<p>${response.data.reason}</p>`;
          content += `<p><strong>推荐医生：</strong></p>`;
          
          response.data.doctors.forEach((doctor, index) => {
            content += `<div class="recommended-doctor">
              <p><strong>${doctor.name}</strong> · ${doctor.title} · ${doctor.hospital}</p>
              <p>${doctor.specialty}</p>
              <p>评分：${doctor.rating} (${doctor.reviewCount}条评价)</p>
            </div>`;
            if (index < response.data.doctors.length - 1) {
              content += '<hr>';
            }
          });
          
          messages.value.push({
            type: 'assistant',
            content: content,
            timestamp: new Date().toISOString()
          });
        }
      } catch (error) {
        messages.value.push({
          type: 'assistant',
          content: '抱歉，获取医生推荐失败，请稍后再试。',
          timestamp: new Date().toISOString()
        });
      } finally {
        loading.value = false;
        scrollToBottom();
      }
    };

    // Get AI direct recommendation
    const getAIDirectRecommendation = async () => {
      if (loading.value || !hasUserMessage.value) return;
      
      loading.value = true;
      
      try {
        const description = getLastUserMessage();
        // 获取当前登录用户ID，如果有的话
        const userId = localStorage.getItem('userId');
        const response = await getAIDirectRecommendations(description, userId, 5);
        
        if (response && response.data) {
          // 从AI直接推荐结果中构建响应内容
          let content = '<div class="ai-recommendation">';
          
          if (Array.isArray(response.data)) {
            // 标准响应格式
            content += `<h4>AI精准推荐医生</h4>`;
            content += `<p>根据您的描述，为您推荐以下医生：</p>`;
            
            response.data.forEach((doctor, index) => {
              content += `<div class="doctor-recommendation">
                <h5>${doctor.name} · ${doctor.positionsName || doctor.title}</h5>
                <p><strong>${doctor.departmentName}</strong> · ${doctor.hospital || '未知医院'}</p>
                <p><strong>专长：</strong>${formatExpertiseDisplay(doctor)}</p>
                <p><strong>匹配度：</strong>${doctor.matchScore * 100}%</p>
                <p><strong>推荐理由：</strong>${doctor.recommendReason}</p>
              </div>`;
              if (index < response.data.length - 1) {
                content += '<hr>';
              }
            });
          } else if (response.data.recommendedDepartment) {
            // 科室和医生推荐格式
            content += `<h4>AI精准推荐</h4>`;
            content += `<p><strong>推荐科室：</strong>${response.data.recommendedDepartment}</p>`;
            content += `<p><strong>推荐理由：</strong>${response.data.reason}</p>`;
            content += `<h5>推荐医生：</h5>`;
            
            response.data.doctors.forEach((doctor, index) => {
              content += `<div class="doctor-recommendation">
                <h5>${doctor.name} · ${doctor.title}</h5>
                <p><strong>${doctor.department}</strong> · ${doctor.hospital}</p>
                <p><strong>专长：</strong>${formatExpertiseDisplay(doctor)}</p>
                <p><strong>评分：</strong>${doctor.rating} (${doctor.reviewCount}条评价)</p>
              </div>`;
              if (index < response.data.doctors.length - 1) {
                content += '<hr>';
              }
            });
          }
          
          content += '</div>';
          
          messages.value.push({
            type: 'assistant',
            content: content,
            timestamp: new Date().toISOString()
          });
        }
      } catch (error) {
        console.error('AI精准推荐失败:', error);
        messages.value.push({
          type: 'assistant',
          content: '抱歉，AI精准推荐失败，请稍后再试。',
          timestamp: new Date().toISOString()
        });
      } finally {
        loading.value = false;
        scrollToBottom();
      }
    };
    
    // Scroll to bottom of chat
    const scrollToBottom = () => {
      nextTick(() => {
        if (messagesContainer.value) {
          messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
        }
      });
    };
    
    // Watch messages and scroll to bottom when new messages are added
    watch(messages, () => {
      scrollToBottom();
    });
    
    // Add welcome message on mount
    onMounted(() => {
      messages.value.push({
        type: 'assistant',
        content: '您好！我是AI医疗助手。请描述您的症状，我可以为您提供初步建议和科室推荐。',
        timestamp: new Date().toISOString()
      });
    });
    
    return {
      inputMessage,
      messages,
      loading,
      hasUserMessage,
      messagesContainer,
      sendMessage,
      getDepartmentRecommendation,
      getDoctorRecommendation,
      getAIDirectRecommendation,
      formatTime,
      formatMessage,
      formatExpertiseDisplay,
      UserFilled,
      Service,
      Position,
      House,
      User,
      Cpu
    };
  }
}
</script>

<style scoped>
.ai-chat-container {
  display: flex;
  flex-direction: column;
  height: 550px;
  border-radius: 12px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
  background-color: #fff;
  overflow: hidden;
  transition: box-shadow 0.3s ease;
}

.ai-chat-container:hover {
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.2);
}

@keyframes slideIn {
  from { transform: translateX(-20px); opacity: 0; }
  to { transform: translateX(0); opacity: 1; }
}

@keyframes slideInRight {
  from { transform: translateX(20px); opacity: 0; }
  to { transform: translateX(0); opacity: 1; }
}

.ai-message {
  animation: slideIn 0.4s ease-out;
}

.user-message {
  animation: slideInRight 0.4s ease-out;
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); }
  100% { transform: scale(1); }
}

.typing-indicator {
  animation: pulse 2s infinite;
}

.input-container button:hover:not(:disabled) {
  animation: pulse 1s infinite;
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 5px 12px rgba(0, 0, 0, 0.15);
  background: linear-gradient(135deg, #50a8ff, #4094ea);
}

/* Loading animation for buttons when processing */
.recommendation-buttons .el-button.is-loading {
  position: relative;
  pointer-events: none;
  opacity: 0.8;
}

.recommendation-buttons .el-button.is-loading::before {
  content: '';
  position: absolute;
  left: 50%;
  top: 50%;
  width: 16px;
  height: 16px;
  margin-left: -8px;
  margin-top: -8px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: #fff;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.chat-header {
  padding: 20px 24px;
  background: linear-gradient(135deg, #409eff, #3a8ee6);
  color: white;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
}

.chat-header::before {
  content: '';
  position: absolute;
  top: -20px;
  right: -20px;
  width: 120px;
  height: 120px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 50%;
  z-index: 0;
}

.chat-header h3 {
  margin: 0 0 6px 0;
  font-size: 1.4rem;
  font-weight: 600;
  position: relative;
  z-index: 1;
}

.subheader {
  margin: 0;
  font-size: 0.95rem;
  opacity: 0.9;
  position: relative;
  z-index: 1;
}

.messages-container {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background-color: #f8fafc;
  background-image: 
    linear-gradient(rgba(255, 255, 255, 0.8) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.8) 1px, transparent 1px);
  background-size: 20px 20px;
  background-position: -1px -1px;
  scrollbar-width: thin;
  scrollbar-color: rgba(64, 158, 255, 0.4) rgba(0, 0, 0, 0.05);
}

/* Webkit browsers like Chrome/Safari/Edge */
.messages-container::-webkit-scrollbar {
  width: 8px;
}

.messages-container::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.05);
  border-radius: 10px;
}

.messages-container::-webkit-scrollbar-thumb {
  background-color: rgba(64, 158, 255, 0.4);
  border-radius: 10px;
  transition: background-color 0.3s;
}

.messages-container::-webkit-scrollbar-thumb:hover {
  background-color: rgba(64, 158, 255, 0.6);
}

.message {
  display: flex;
  margin-bottom: 24px;
  max-width: 85%;
  animation: fadeIn 0.3s ease-in-out;
  transform-origin: bottom;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px) scale(0.98); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

.user-message {
  margin-left: auto;
  flex-direction: row-reverse;
}

.ai-message {
  margin-right: auto;
}

.message-avatar {
  width: 40px;
  height: 40px;
  margin: 0 10px;
  flex-shrink: 0;
  position: relative;
  z-index: 1;
}

.user-avatar {
  background: linear-gradient(135deg, #4a8cfc, #356ac1);
  color: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
}

.ai-avatar {
  background: linear-gradient(135deg, #42b983, #2d8b5f);
  color: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
}

.message-content {
  background-color: white;
  padding: 14px 18px;
  border-radius: 18px;
  box-shadow: 0 3px 12px rgba(0, 0, 0, 0.08);
  position: relative;
  transition: transform 0.2s;
}

.message-content:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.user-message .message-content {
  background: linear-gradient(135deg, #409eff, #3a8ee6);
  color: white;
  border-top-right-radius: 4px;
  box-shadow: 0 3px 12px rgba(58, 142, 230, 0.2);
}

.ai-message .message-content {
  background-color: white;
  border-top-left-radius: 4px;
  box-shadow: 0 3px 12px rgba(0, 0, 0, 0.08);
}

.message-content::before {
  content: '';
  position: absolute;
  width: 0;
  height: 0;
  border: 8px solid transparent;
  z-index: 0;
}

.ai-message .message-content::before {
  top: 12px;
  left: -8px;
  border-right-color: white;
  border-left: 0;
}

.user-message .message-content::before {
  top: 12px;
  right: -8px;
  border-left-color: #409eff;
  border-right: 0;
}

.message-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
  font-size: 0.8rem;
}

.sender-name {
  font-weight: 600;
}

.message-time {
  opacity: 0.7;
}

.user-message .message-time {
  color: rgba(255, 255, 255, 0.8);
}

.message-text {
  word-break: break-word;
  line-height: 1.5;
}

.loading-indicator {
  display: flex;
  justify-content: flex-start;
  margin-bottom: 20px;
}

.typing-indicator {
  background-color: #f0f0f0;
  border-radius: 50px;
  padding: 12px 20px;
  display: inline-flex;
  align-items: center;
}

.typing-indicator span {
  height: 8px;
  width: 8px;
  margin: 0 2px;
  background-color: #9E9EA1;
  display: block;
  border-radius: 50%;
  opacity: 0.4;
  animation: typing 1.5s infinite;
}

.typing-indicator span:nth-child(1) {
  animation-delay: 0s;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0% {
    opacity: 0.4;
    transform: scale(1);
  }
  50% {
    opacity: 0.8;
    transform: scale(1.2);
  }
  100% {
    opacity: 0.4;
    transform: scale(1);
  }
}

.chat-input {
  padding: 20px;
  background-color: white;
  border-top: 1px solid #e8eaed;
  position: relative;
}

.chat-input :deep(.el-textarea__inner) {
  min-height: 60px !important;
  padding: 12px 15px;
  font-size: 15px;
  border-radius: 8px;
  border: 1px solid #dcdfe6;
  transition: all 0.3s;
  line-height: 1.6;
  resize: none;
}

.chat-input :deep(.el-textarea__inner:focus) {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.chat-input :deep(.el-input-group__append) {
  padding: 0;
  border: none;
  background: transparent;
}

.send-button {
  height: 100%;
  min-width: 80px;
  border-radius: 8px;
  padding: 12px 24px;
  font-size: 16px;
  font-weight: 500;
  margin-left: 10px;
  background: linear-gradient(135deg, #409eff, #3a8ee6);
  transition: all 0.3s;
}

.send-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  background: linear-gradient(135deg, #50a8ff, #4094ea);
}

.send-button:active {
  transform: translateY(0);
  box-shadow: 0 2px 6px rgba(64, 158, 255, 0.2);
}

.input-tip {
  position: absolute;
  right: 25px;
  bottom: 5px;
  font-size: 12px;
  color: #909399;
  opacity: 0.8;
}

.recommendation-buttons {
  display: flex;
  justify-content: center;
  padding: 16px;
  gap: 14px;
  border-top: 1px solid #e8eaed;
  background-color: #f8f9fa;
  flex-wrap: wrap;
  position: relative;
}

.recommendation-buttons::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(to right, 
    rgba(64, 158, 255, 0),
    rgba(64, 158, 255, 0.5),
    rgba(64, 158, 255, 0)
  );
}

.recommendation-buttons button {
  transition: all 0.3s ease;
  transform-origin: center;
  border-radius: 8px;
  border: none;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
}

.recommendation-buttons .el-button {
  padding: 10px 18px;
}

.recommendation-buttons .el-button .el-icon {
  font-size: 16px;
  margin-right: 5px;
}

.recommendation-buttons button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.recommendation-buttons button:active:not(:disabled) {
  transform: scale(0.98);
}

.recommended-doctor {
  margin: 12px 0;
  padding: 14px;
  background-color: #f5f8fa;
  border-radius: 8px;
  border-left: 3px solid #409eff;
}

.recommended-doctor p {
  margin: 6px 0;
}

hr {
  border: none;
  border-top: 1px solid #e8eaed;
  margin: 12px 0;
}

.ai-recommendation {
  background-color: #f0f9eb;
  border-radius: 12px;
  padding: 18px;
  border-left: 4px solid #67c23a;
  margin: 12px 0;
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.1);
}

.ai-recommendation h4 {
  color: #67c23a;
  margin-top: 0;
  margin-bottom: 14px;
  font-size: 18px;
  display: flex;
  align-items: center;
}

.ai-recommendation h4:before {
  content: '✓';
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  background-color: #67c23a;
  color: white;
  border-radius: 50%;
  margin-right: 8px;
  font-size: 14px;
  font-weight: bold;
}

.ai-recommendation h5 {
  margin: 16px 0 8px;
  color: #303133;
  font-size: 16px;
  border-bottom: 1px dashed #dcdfe6;
  padding-bottom: 8px;
}

.doctor-recommendation {
  background-color: white;
  border-radius: 10px;
  padding: 16px;
  margin: 16px 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border-left: 3px solid #409eff;
  transition: transform 0.3s, box-shadow 0.3s;
}

.doctor-recommendation:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
}

.doctor-recommendation h5 {
  margin: 0 0 10px;
  color: #409eff;
  font-size: 16px;
  display: flex;
  align-items: center;
}

.doctor-recommendation h5:before {
  content: '👨‍⚕️';
  margin-right: 8px;
  font-size: 18px;
}

.doctor-recommendation p {
  margin: 8px 0;
  line-height: 1.5;
}

@media screen and (max-width: 768px) {
  .ai-chat-container {
    height: calc(100vh - 160px);
    border-radius: 0;
  }
  
  .message-content {
    max-width: 85%;
  }
  
  .recommendation-buttons {
    flex-direction: column;
    align-items: stretch;
  }
  
  .recommendation-buttons button {
    max-width: none;
  }
}
</style> 