<template>
    <el-card shadow="hover">
        <template #header>
        統計圖表 (月)
        </template>

        <Bar v-if="chartData" :data="chartData" :option="chartOptions"></Bar>
        <p v-else>載入中</p>
    </el-card>
</template>

<script setup>
import {ref, onMounted} from 'vue'
import {Bar} from 'vue-chartjs'
import {
    Chart as ChartJS,
    Title,
    Tooltip,
    Legend,
    BarElement,
    CategoryScale,
    LinearScale
} from 'chart.js'
import api from '@/services/api'

// 註冊 Chart.js 模組
ChartJS.register(Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale)

const chartData = ref(null)

const chartOptions = {
  responsive: true,
  plugins: {
    legend: { display: false },
    title: { display: true, text: '月採購金額 (元)' }
  }
}

const fetchChartData = async () => {
  try{
  const res = await api.get('/api/order/amount-per-month')
  const data = res.data

  chartData.value = {
    labels: data.map(d => d.period),
    datasets: [
      {
        label: '採購金額',
        data: data.map(d => d.totalAmount),
        backgroundColor: '#409EFF'
      }
    ]
  }
}catch(error){
 console.error('載入失敗')
}
}
onMounted(fetchChartData)
</script>