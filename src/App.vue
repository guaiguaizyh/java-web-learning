<script setup>
import { RouterView, useRouter, useRoute } from 'vue-router'
import { computed, ref, onMounted } from 'vue'

const router = useRouter();
const route = useRoute();

const showAIFloat = computed(() => {
  // 在AI聊天页面、登录页和注册页不显示
  const hideOnPaths = ['/ai-chat', '/login', '/register'];
  // 在管理员端（路径包含 /admin/）不显示
  if (route.path.includes('/admin/')) {
    return false;
  }
  return !hideOnPaths.includes(route.path);
});

const goToAIChat = (event) => {
  // 如果是拖动操作，不跳转
  if (wasDragging.value) {
    return;
  }
  router.push('/ai-chat');
};

// 悬浮球拖动相关
const isDragging = ref(false);
const wasDragging = ref(false);
const position = ref({ x: 30, y: 100 });
const startPosition = ref({ x: 0, y: 0 });
const offset = ref({ x: 0, y: 0 });
const dragStartTime = ref(0);

// 开始拖动
const startDrag = (event) => {
  isDragging.value = true;
  wasDragging.value = false;
  dragStartTime.value = Date.now();
  
  startPosition.value = {
    x: event.clientX,
    y: event.clientY
  };
  offset.value = {
    x: position.value.x,
    y: position.value.y
  };
  
  // 添加全局事件
  document.addEventListener('mousemove', onDrag);
  document.addEventListener('mouseup', endDrag);
};

// 拖动中
const onDrag = (event) => {
  if (!isDragging.value) return;
  
  // 标记为正在拖动
  wasDragging.value = true;
  
  // 计算新位置
  position.value = {
    x: offset.value.x + (event.clientX - startPosition.value.x),
    y: offset.value.y + (event.clientY - startPosition.value.y)
  };
  
  // 限制边界
  const bubble = document.querySelector('.ai-float-bubble');
  if (bubble) {
    const bubbleRect = bubble.getBoundingClientRect();
    const windowWidth = window.innerWidth;
    const windowHeight = window.innerHeight;
    
    // 限制在窗口内
    if (position.value.x < 0) position.value.x = 0;
    if (position.value.y < 0) position.value.y = 0;
    if (position.value.x > windowWidth - bubbleRect.width) {
      position.value.x = windowWidth - bubbleRect.width;
    }
    if (position.value.y > windowHeight - bubbleRect.height) {
      position.value.y = windowHeight - bubbleRect.height;
    }
  }
};

// 结束拖动
const endDrag = () => {
  isDragging.value = false;
  document.removeEventListener('mousemove', onDrag);
  document.removeEventListener('mouseup', endDrag);
  
  // 短时间内的点击不算拖动
  if (Date.now() - dragStartTime.value < 200 && !wasDragging.value) {
    wasDragging.value = false;
  }
  
  // 延迟重置拖动状态，让click事件能够检查到
  setTimeout(() => {
    wasDragging.value = false;
  }, 100);
};

// 组件挂载时，设置初始位置
onMounted(() => {
  // 将悬浮球初始位置设置在右侧中间位置
  position.value = {
    x: window.innerWidth - 100,
    y: window.innerHeight / 2
  };
});
</script>

<template>
  <router-view></router-view>
  
  <div 
    v-if="showAIFloat" 
    class="ai-float-bubble"
    @mousedown="startDrag"
    @click="goToAIChat"
    :style="{
      right: 'auto',
      bottom: 'auto',
      left: position.x + 'px',
      top: position.y + 'px'
    }"
  >
    <div class="ai-bubble-inner">
      <div class="ai-bubble-face">
        <div class="ai-eyes">
          <div class="ai-eye"></div>
          <div class="ai-eye"></div>
        </div>
        <div class="ai-smile"></div>
      </div>
    </div>
    <div class="ai-tooltip">你的AI客服~<br><small>(可拖动)</small></div>
  </div>
</template>

<style>
html, body {
  margin: 0;
  padding: 0;
  height: 100%;
}

#app {
  height: 100%;
}

.ai-float-bubble {
  position: fixed;
  width: 70px;
  height: 70px;
  border-radius: 50%;
  background-color: #409eff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: grab;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  z-index: 9999;
  transition: transform 0.3s;
  user-select: none;
}

.ai-float-bubble:active {
  cursor: grabbing;
}

.ai-float-bubble:hover {
  transform: scale(1.1);
}

.ai-float-bubble:hover .ai-tooltip {
  opacity: 1;
  transform: translateX(-20px);
}

.ai-bubble-inner {
  width: 85%;
  height: 85%;
  border-radius: 50%;
  background-color: white;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ai-bubble-face {
  width: 70%;
  height: 70%;
  position: relative;
}

.ai-eyes {
  display: flex;
  justify-content: space-around;
  position: absolute;
  width: 100%;
  top: 28%;
}

.ai-eye {
  width: 12px;
  height: 8px;
  border-radius: 50%;
  background-color: #409eff;
  position: relative;
}

.ai-eye:before {
  content: '';
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border-top: 3px solid #409eff;
  top: -3px;
  left: 0;
}

.ai-smile {
  position: absolute;
  bottom: 25%;
  left: 22%;
  width: 56%;
  height: 15px;
  border-bottom: 4px solid #409eff;
  border-radius: 0 0 50px 50px;
}

.ai-tooltip {
  position: absolute;
  left: -150px;
  background-color: white;
  padding: 8px 15px;
  border-radius: 20px;
  font-size: 14px;
  color: #409eff;
  font-weight: bold;
  opacity: 0;
  transform: translateX(10px);
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  white-space: nowrap;
  text-align: center;
  pointer-events: none;
}

.ai-tooltip small {
  font-size: 12px;
  opacity: 0.8;
}

.ai-tooltip:after {
  content: '';
  position: absolute;
  top: 50%;
  right: -10px;
  transform: translateY(-50%);
  border-width: 10px 0 10px 10px;
  border-style: solid;
  border-color: transparent transparent transparent white;
}
</style>
