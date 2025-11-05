<template>
  <div style="display: flex; gap: 20px; flex-wrap: wrap;">
    <MonthlyBarChart style="flex: 1; min-width: 400px;" />
    <SupplierPieChart style="flex: 1; min-width: 400px;" />
  </div>
</template>

<script setup>
import MonthlyBarChart from '@/views/zt/MonthlyBarChart.vue'
import SupplierPieChart from '@/views/zt/SupplierPieChart.vue'
</script>
