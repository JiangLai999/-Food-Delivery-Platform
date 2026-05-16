<template>
  <div class="brand-culture">
    <div class="culture-header">
      <img src="/src/assets/logo.png" alt="秒送餐" class="brand-logo" />
      <div class="brand-info">
        <h1>秒送餐</h1>
        <p class="slogan">快人一步，美味即达</p>
        <p class="subtitle">专业的即时配送品牌，为美好生活提速</p>
      </div>
    </div>

    <div class="culture-section">
      <div class="culture-card brand-vision">
        <div class="card-icon">🎯</div>
        <h3>我们的愿景</h3>
        <p>成为最受信赖的即时配送服务平台，让每一位用户都能享受到高效、便捷、美好的生活服务体验</p>
      </div>

      <div class="culture-card brand-mission">
        <div class="card-icon">🚀</div>
        <h3>我们的使命</h3>
        <p>以科技创新为驱动，以品质服务为根本，为商家和用户搭建最优质的配送桥梁，创造共同的商业价值</p>
      </div>

      <div class="culture-card brand-values">
        <div class="card-icon">💎</div>
        <h3>核心价值</h3>
        <ul>
          <li><strong>诚信</strong> - 说到做到，不负所托</li>
          <li><strong>高效</strong> - 分秒必争，使命必达</li>
          <li><strong>创新</strong> - 持续突破，引领未来</li>
          <li><strong>共赢</strong> - 携手并进，共享成果</li>
        </ul>
      </div>
    </div>

    <div class="culture-story">
      <h2>品牌故事</h2>
      <p>
        秒送餐成立于2020年，从一家小小的配送站起步，凭借着"快人一步"的服务理念和"美味即达"的服务质量，
        迅速成长为城市配送领域的标杆企业。我们始终相信，配送不仅仅是服务，更是一种生活态度。
        每一个订单背后，都是一份信任；每一次送达，都是一次承诺。
      </p>
    </div>

    <div class="culture-stats">
      <div class="stat-item">
        <div class="stat-value">100万+</div>
        <div class="stat-label">服务用户</div>
      </div>
      <div class="stat-item">
        <div class="stat-value">10000+</div>
        <div class="stat-label">合作商家</div>
      </div>
      <div class="stat-item">
        <div class="stat-value">5000+</div>
        <div class="stat-label">专职骑手</div>
      </div>
      <div class="stat-item">
        <div class="stat-value">99.9%</div>
        <div class="stat-label">准时送达率</div>
      </div>
    </div>

    <div class="culture-divider">
      <span>💫</span>
      <span>用专业守护美味，用速度传递温暖</span>
      <span>💫</span>
    </div>
  </div>
</template>

<script setup>
// 品牌文化展示组件
</script>

<style scoped>
.brand-culture {
  padding: 60px 40px;
  background: linear-gradient(135deg, #1A202C 0%, #2D3748 100%);
  color: white;
  position: relative;
  overflow: hidden;
}

.brand-culture::before {
  content: '';
  position: absolute;
  top: -100px;
  right: -100px;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(245, 158, 11, 0.1) 0%, transparent 70%);
}

.brand-culture::after {
  content: '';
  position: absolute;
  bottom: -100px;
  left: -100px;
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(245, 158, 11, 0.08) 0%, transparent 70%);
}

.culture-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 32px;
  margin-bottom: 60px;
  position: relative;
  z-index: 1;
}

.brand-logo {
  width: 120px;
  height: 120px;
  border-radius: 24px;
  box-shadow: 0 8px 32px rgba(245, 158, 11, 0.3);
}

.brand-info {
  text-align: center;
}

.brand-info h1 {
  font-size: 48px;
  font-weight: 800;
  margin: 0 0 12px 0;
  background: linear-gradient(135deg, #F59E0B 0%, #FBBF24 50%, #D69E2E 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.slogan {
  font-size: 28px;
  font-weight: 600;
  color: #F59E0B;
  margin: 0 0 8px 0;
}

.subtitle {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.7);
  margin: 0;
}

.culture-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 32px;
  margin-bottom: 60px;
  position: relative;
  z-index: 1;
}

.culture-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  padding: 32px;
  transition: all 0.3s ease;
}

.culture-card:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(245, 158, 11, 0.3);
  transform: translateY(-8px);
  box-shadow: 0 12px 40px rgba(245, 158, 11, 0.2);
}

.card-icon {
  font-size: 48px;
  margin-bottom: 20px;
}

.culture-card h3 {
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 16px 0;
  color: #F59E0B;
}

.culture-card p {
  font-size: 16px;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.8);
  margin: 0;
}

.brand-values ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.brand-values li {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 12px;
  padding-left: 24px;
  position: relative;
  line-height: 1.6;
}

.brand-values li::before {
  content: '★';
  position: absolute;
  left: 0;
  color: #F59E0B;
}

.brand-values li strong {
  color: #FBBF24;
  font-weight: 600;
}

.culture-story {
  text-align: center;
  max-width: 800px;
  margin: 0 auto 60px;
  position: relative;
  z-index: 1;
}

.culture-story h2 {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 24px;
  color: #F59E0B;
}

.culture-story p {
  font-size: 16px;
  line-height: 2;
  color: rgba(255, 255, 255, 0.8);
}

.culture-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  margin-bottom: 60px;
  position: relative;
  z-index: 1;
}

.stat-item {
  text-align: center;
  padding: 24px;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
}

.stat-value {
  font-size: 36px;
  font-weight: 800;
  color: #F59E0B;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
}

.culture-divider {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
  font-size: 24px;
  color: #F59E0B;
  position: relative;
  z-index: 1;
  padding: 20px;
  border-top: 1px solid rgba(245, 158, 11, 0.2);
  border-bottom: 1px solid rgba(245, 158, 11, 0.2);
  margin-top: 60px;
}

.culture-divider span {
  opacity: 0.8;
}
</style>
