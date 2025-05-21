import request from '@/utils/axios';

/**
 * AI Chat API service
 */

// Send chat message to AI
export const sendChatMessage = async (message, lastContext = '') => {
  try {
    const response = await request.post('/ai/chat', { 
      message,
      lastContext  // 添加上下文参数
    });
    return response;
  } catch (error) {
    console.error('Error sending chat message:', error);
    throw error;
  }
};

// Get department recommendation based on symptoms
export const getDepartmentRecommendation = async (symptoms) => {
  try {
    const response = await request.post('/ai/recommend-department', { symptoms });
    return response;
  } catch (error) {
    console.error('Error getting department recommendation:', error);
    throw error;
  }
};

// Get doctor recommendations based on symptoms
export const getDoctorRecommendations = async (symptoms, limit = 3) => {
  try {
    const response = await request.post('/ai/recommend-doctors', { 
      symptoms,
      limit
    });
    return response;
  } catch (error) {
    console.error('Error getting doctor recommendations:', error);
    throw error;
  }
};

/**
 * Doctor Recommendation APIs
 */

// Get recommendations using natural language processing
export const getNaturalLanguageRecommendations = async (description, userId, limit = 10) => {
  try {
    const response = await request.post('/recommend/doctors', {
      description,
      userId,
      limit
    });
    return response;
  } catch (error) {
    console.error('Error getting natural language recommendations:', error);
    throw error;
  }
};

// Get recommendations using real-time symptom input
export const getSymptomRecommendations = async (symptomDescription, userId, limit = 10) => {
  try {
    const response = await request.post('/recommend/doctors/symptom', {
      symptomDescription,
      userId,
      limit
    });
    return response;
  } catch (error) {
    console.error('Error getting symptom recommendations:', error);
    throw error;
  }
};

// Get recommendations from user profile data
export const getUserProfileRecommendations = async (userId, limit = 10) => {
  try {
    const response = await request.get(`/recommend/doctors/user-profile?userId=${userId}&limit=${limit}`);
    return response;
  } catch (error) {
    console.error('Error getting user profile recommendations:', error);
    throw error;
  }
};

// Get AI direct recommendations
export const getAIDirectRecommendations = async (description, userId, limit = 5) => {
  try {
    const response = await request.post('/recommend/doctors', {
      description,
      userId,
      limit,
      recommendType: 'AI'  // 使用AI推荐方式
    });
    return response;
  } catch (error) {
    console.error('Error getting AI direct recommendations:', error);
    throw error;
  }
};

// Get recommendations using unified recommendation API (normal strategy)
export const getNormalRecommendations = async (description, userId, limit = 8) => {
  try {
    const response = await request.post('/recommend/doctors', {
      description,
      userId,
      limit,
      strategy: 'normal' // 使用普通推荐策略
    });
    return response;
  } catch (error) {
    console.error('Error getting normal recommendations:', error);
    throw error;
  }
};