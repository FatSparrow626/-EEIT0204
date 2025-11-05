<template>
  <el-card shadow="hover">
    <template #header>
       各供應商採購佔比
    </template>

    <Pie v-if="chartData" :data="chartData" :options="chartOptions" />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Pie } from 'vue-chartjs'
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  ArcElement
} from 'chart.js'
import api from '@/services/api'

// 註冊 Chart.js 模組
ChartJS.register(Title, Tooltip, Legend, ArcElement)

const chartData = ref(null)

const chartOptions = {
  responsive: true,
  plugins: {
    legend: { position: 'right' },
    title: { display: true, text: '供應商佔比 (%)' }
  }
}

const fetchChartData = async () => {
  const res = await api.get('/api/order/supplier-ratio')
  const data = res.data

  chartData.value = {
    labels: data.map(d => d.supplierName),
    datasets: [
      {
        label: '採購金額',
        data: data.map(d => d.totalAmount),
        backgroundColor: [
          '#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF'
        ]
      }
    ]
  }
}

onMounted(fetchChartData)
</script>
