<template>
  <div class="flex flex-col gap-6 py-6 pr-6">

    <!-- ── Header ─────────────────────────────────────────────────────────── -->
    <div class="flex flex-col gap-2">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-lg font-semibold text-text-admin-primary">
            Dashboard
          </h1>
          <p class="text-sm text-text-admin-tertiary">
            {{ currentDateLabel }}
          </p>
        </div>

        <!-- Cinema Filter -->
        <div class="flex items-center gap-3">
          <span class="text-sm text-text-admin-secondary">Chi nhánh:</span>
          <select
            v-model="selectedCinemaId"
            @change="onCinemaChange"
            class="px-3 py-2 rounded-lg text-sm border bg-white border-gray-200 text-gray-800 focus:outline-none focus:ring-2 focus:ring-gold-400 cursor-pointer"
          >
            <option :value="null">Tất cả chi nhánh</option>
            <option
              v-for="cinema in cinemas"
              :key="cinema.id"
              :value="cinema.id"
            >
              {{ cinema.name }}
            </option>
          </select>

          <button
            @click="refresh"
            :disabled="isLoading"
            class="flex items-center gap-1.5 px-3 py-2 rounded-lg text-sm bg-gray-800 text-white hover:bg-gray-700 transition-colors disabled:opacity-50"
          >
            <RefreshCw class="size-4" :class="{ 'animate-spin': isLoading }" />
            Làm mới
          </button>
        </div>
      </div>
    </div>

    <!-- ── Loading Overlay ───────────────────────────────────────────────── -->
    <LoadingOverlay v-if="isLoading" />

    <!-- ── Metric Cards ──────────────────────────────────────────────────── -->
    <div v-if="data" class="grid grid-cols-2 xl:grid-cols-4 gap-4">
      <MetricCard
        :label="data.todayRevenue.label"
        :value="data.todayRevenue.value"
        :change="data.todayRevenue.changePercent"
        :prefix="data.todayRevenue.prefix ?? undefined"
        icon="revenue"
        theme="gold"
      />
      <MetricCard
        :label="data.ticketsSold.label"
        :value="data.ticketsSold.value"
        :change="data.ticketsSold.changePercent"
        :suffix="data.ticketsSold.suffix ?? undefined"
        icon="ticket"
        theme="violet"
      />
      <MetricCard
        :label="data.occupancyRate.label"
        :value="data.occupancyRate.value"
        :change="data.occupancyRate.changePercent"
        :suffix="data.occupancyRate.suffix ?? undefined"
        icon="seat"
        theme="emerald"
      />
      <MetricCard
        :label="data.newUsers.label"
        :value="data.newUsers.value"
        :change="data.newUsers.changePercent"
        :suffix="data.newUsers.suffix ?? undefined"
        icon="user"
        theme="amber"
      />
    </div>

    <!-- ── Mock Metric Cards (when no data) ─────────────────────────────── -->
    <div v-else class="grid grid-cols-2 xl:grid-cols-4 gap-4">
      <SkeletonCard v-for="i in 4" :key="i" />
    </div>

    <!-- ── Charts Row ─────────────────────────────────────────────────────── -->
    <div v-if="data" class="grid grid-cols-1 xl:grid-cols-3 gap-4">
      <!-- Line Chart: Revenue Trend -->
      <div class="xl:col-span-2 bg-white rounded-xl border border-gray-200 p-5 shadow-sm">
        <div class="flex items-center justify-between mb-4">
          <div>
            <h3 class="text-base font-semibold text-gray-800">Doanh thu theo giờ</h3>
            <p class="text-xs text-gray-500 mt-0.5">Biến động doanh thu trong ngày</p>
          </div>
          <div class="flex items-center gap-2">
            <span class="flex items-center gap-1 text-xs text-gold-500">
              <span class="w-2.5 h-2.5 rounded-full bg-gold-500 inline-block"></span>
              Doanh thu
            </span>
          </div>
        </div>
        <div class="h-52">
          <Line :data="lineChartData" :options="lineChartOptions" />
        </div>
      </div>

      <!-- Pie Chart: Revenue Structure -->
      <div class="bg-white rounded-xl border border-gray-200 p-5 shadow-sm">
        <div class="mb-4">
          <h3 class="text-base font-semibold text-gray-800">Cơ cấu doanh thu</h3>
          <p class="text-xs text-gray-500 mt-0.5">Vé vs F&B hôm nay</p>
        </div>
        <div class="h-40 flex items-center justify-center">
          <Doughnut :data="pieChartData" :options="pieChartOptions" />
        </div>
        <div class="flex flex-col gap-2 mt-4">
          <div class="flex items-center justify-between text-sm">
            <span class="flex items-center gap-2">
              <span class="w-2.5 h-2.5 rounded-full bg-violet-500 inline-block"></span>
              <span class="text-gray-600">Vé</span>
            </span>
            <span class="font-medium text-gray-800">
              {{ data.revenueStructure.ticketPercent }}%
            </span>
          </div>
          <div class="flex items-center justify-between text-sm">
            <span class="flex items-center gap-2">
              <span class="w-2.5 h-2.5 rounded-full bg-gold-500 inline-block"></span>
              <span class="text-gray-600">F&B</span>
            </span>
            <span class="font-medium text-gray-800">
              {{ data.revenueStructure.fnbPercent }}%
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- ── Bar Chart + Tables Row ───────────────────────────────────────── -->
    <div v-if="data" class="grid grid-cols-1 xl:grid-cols-3 gap-4">

      <!-- Bar Chart: Top 5 Movies -->
      <div class="xl:col-span-2 bg-white rounded-xl border border-gray-200 p-5 shadow-sm">
        <div class="flex items-center justify-between mb-4">
          <div>
            <h3 class="text-base font-semibold text-gray-800">Top 5 Phim hot nhất</h3>
            <p class="text-xs text-gray-500 mt-0.5">Xếp hạng theo số vé bán ra hôm nay</p>
          </div>
          <div class="flex items-center gap-2">
            <span class="flex items-center gap-1 text-xs text-violet-500">
              <span class="w-2.5 h-2.5 rounded-full bg-violet-500 inline-block"></span>
              Số vé
            </span>
          </div>
        </div>
        <div class="h-52">
          <Bar :data="barChartData" :options="barChartOptions" />
        </div>
      </div>

      <!-- Cinema Status List -->
      <div class="bg-white rounded-xl border border-gray-200 p-5 shadow-sm flex flex-col">
        <div class="mb-4">
          <h3 class="text-base font-semibold text-gray-800">Trạng thái chi nhánh</h3>
          <p class="text-xs text-gray-500 mt-0.5">Giám sát hoạt động theo thời gian thực</p>
        </div>
        <div class="flex-1 flex flex-col gap-2 overflow-y-auto max-h-52">
          <div
            v-for="cinema in data.cinemaStatuses"
            :key="cinema.cinemaId"
            class="flex items-center justify-between py-2 px-3 rounded-lg bg-gray-50 border border-gray-100"
          >
            <div class="flex items-center gap-2.5">
              <span
                class="w-2.5 h-2.5 rounded-full flex-shrink-0"
                :class="cinema.status === 'ACTIVE' ? 'bg-emerald-500' : 'bg-red-400'"
              ></span>
              <div class="flex flex-col">
                <span class="text-sm font-medium text-gray-800 leading-tight">{{ cinema.cinemaName }}</span>
                <span class="text-xs text-gray-500">{{ cinema.status === 'ACTIVE' ? 'Hoạt động' : 'Bảo trì' }}</span>
              </div>
            </div>
            <span
              class="text-xs font-medium px-2 py-0.5 rounded-full"
              :class="cinema.status === 'ACTIVE'
                ? 'bg-emerald-50 text-emerald-600'
                : 'bg-red-50 text-red-500'"
            >
              {{ cinema.status === 'ACTIVE' ? 'Online' : 'Offline' }}
            </span>
          </div>
          <div v-if="data.cinemaStatuses.length === 0" class="text-center text-sm text-gray-400 py-4">
            Không có dữ liệu chi nhánh
          </div>
        </div>
      </div>
    </div>

    <!-- ── Upcoming Showtimes Table ──────────────────────────────────────── -->
    <div v-if="data" class="bg-white rounded-xl border border-gray-200 p-5 shadow-sm">
      <div class="flex items-center justify-between mb-4">
        <div>
          <h3 class="text-base font-semibold text-gray-800">Suất chiếu sắp tới</h3>
          <p class="text-xs text-gray-500 mt-0.5">Các suất chiếu trong 2 giờ tới</p>
        </div>
        <span class="text-xs text-gray-400 bg-gray-100 px-2 py-1 rounded-full">
          {{ data.upcomingShowtimes.length }} suất
        </span>
      </div>
      <div class="overflow-y-auto max-h-64">
        <table class="w-full text-sm">
          <thead>
            <tr class="border-b border-gray-200 sticky top-0 bg-white z-10">
              <th class="text-left py-3 px-3 font-medium text-gray-500 text-xs uppercase tracking-wide">Giờ chiếu</th>
              <th class="text-left py-3 px-3 font-medium text-gray-500 text-xs uppercase tracking-wide">Phim</th>
              <th class="text-left py-3 px-3 font-medium text-gray-500 text-xs uppercase tracking-wide">Phòng</th>
              <th class="text-left py-3 px-3 font-medium text-gray-500 text-xs uppercase tracking-wide">Chi nhánh</th>
              <th class="text-center py-3 px-3 font-medium text-gray-500 text-xs uppercase tracking-wide">Ghế đã đặt</th>
              <th class="text-left py-3 px-3 font-medium text-gray-500 text-xs uppercase tracking-wide">Tình trạng</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="showtime in data.upcomingShowtimes"
              :key="showtime.showtimeId"
              class="border-b border-gray-100 hover:bg-gray-50 transition-colors"
            >
              <td class="py-3 px-3">
                <span class="font-mono font-medium text-gray-800">{{ formatTime(showtime.startTime) }}</span>
              </td>
              <td class="py-3 px-3">
                <span class="font-medium text-gray-800">{{ showtime.movieTitle }}</span>
              </td>
              <td class="py-3 px-3">
                <span class="text-gray-600">{{ showtime.screenRoom }}</span>
              </td>
              <td class="py-3 px-3">
                <span class="text-gray-600">{{ showtime.cinemaBranch }}</span>
              </td>
              <td class="py-3 px-3 text-center">
                <div class="flex items-center justify-center gap-2">
                  <div class="w-16 h-1.5 bg-gray-200 rounded-full overflow-hidden">
                    <div
                      class="h-full bg-gold-500 rounded-full"
                      :style="{ width: calcSeatPercent(showtime.bookedSeats, showtime.totalSeats) + '%' }"
                    ></div>
                  </div>
                  <span class="text-xs text-gray-500 font-mono">{{ showtime.bookedRatio }}</span>
                </div>
              </td>
              <td class="py-3 px-3">
                <span
                  class="text-xs font-medium px-2 py-0.5 rounded-full"
                  :class="getSeatStatusClass(showtime.bookedSeats, showtime.totalSeats)"
                >
                  {{ getSeatStatus(showtime.bookedSeats, showtime.totalSeats) }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-if="data.upcomingShowtimes.length === 0" class="text-center text-sm text-gray-400 py-8">
          Không có suất chiếu sắp tới
        </div>
      </div>
    </div>

    <!-- ── Error State ──────────────────────────────────────────────────── -->
    <div
      v-if="error && !isLoading"
      class="rounded-xl bg-red-50 border border-red-200 p-6 text-center"
    >
      <p class="text-sm text-red-600">{{ error }}</p>
      <button
        @click="refresh"
        class="mt-3 text-sm text-red-600 underline hover:no-underline"
      >
        Thử lại
      </button>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  ArcElement,
  Title,
  Tooltip,
  Legend,
  Filler,
} from 'chart.js'
import { Line, Bar, Doughnut } from 'vue-chartjs'
import { RefreshCw, TrendingUp, TrendingDown } from 'lucide-vue-next'
import LoadingOverlay from '@/components/ui/LoadingOverlay.vue'
import { useDashboard } from '@/composables/useDashboard'
import { useCinema } from '@/composables/useCinema'
import type { HourlyRevenueDto, TopMovieDto, UpcomingShowtimeDto } from '@/types/dashboard.types'

// Register Chart.js components
ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  ArcElement,
  Title,
  Tooltip,
  Legend,
  Filler,
)

// ── Stores & Composables ───────────────────────────────────────────────────────
const { data, isLoading, error, fetchDashboard } = useDashboard()
const { fetchAll: fetchAllCinemas } = useCinema()

const cinemas = ref<Array<{ id: number; name: string }>>([])
const selectedCinemaId = ref<number | null>(null)

// ── Date Helpers ───────────────────────────────────────────────────────────────
const currentDateLabel = computed(() => {
  const now = new Date()
  return now.toLocaleDateString('vi-VN', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  })
})

function formatTime(isoString: string): string {
  if (!isoString) return '--:--'
  const date = new Date(isoString)
  return date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })
}

function calcSeatPercent(booked: number, total: number): number {
  if (!total) return 0
  return Math.round((booked / total) * 100)
}

function getSeatStatus(booked: number, total: number): string {
  if (!total) return 'Chưa bán'
  const pct = calcSeatPercent(booked, total)
  if (pct >= 90) return 'Gần kín'
  if (pct >= 60) return 'Còn ghế'
  if (pct >= 30) return 'Nhiều ghế'
  return 'Còn trống'
}

function getSeatStatusClass(booked: number, total: number): string {
  if (!total) return 'bg-gray-100 text-gray-500'
  const pct = calcSeatPercent(booked, total)
  if (pct >= 90) return 'bg-red-50 text-red-500'
  if (pct >= 60) return 'bg-amber-50 text-amber-600'
  if (pct >= 30) return 'bg-blue-50 text-blue-600'
  return 'bg-emerald-50 text-emerald-600'
}

// ── Cinema Filter ─────────────────────────────────────────────────────────────
async function onCinemaChange() {
  await fetchDashboard(selectedCinemaId.value)
}

async function refresh() {
  await fetchDashboard(selectedCinemaId.value)
}

// ── Chart Helpers ─────────────────────────────────────────────────────────────
const lineChartData = computed(() => {
  if (!data.value) return { labels: [], datasets: [] }
  const hours = data.value.hourlyRevenue.map((h: HourlyRevenueDto) => `${h.hour}h`)
  const revenues = data.value.hourlyRevenue.map((h: HourlyRevenueDto) => h.revenue)

  return {
    labels: hours,
    datasets: [
      {
        label: 'Doanh thu',
        data: revenues,
        borderColor: '#c9a74e',
        backgroundColor: 'rgba(201, 167, 78, 0.1)',
        fill: true,
        tension: 0.4,
        pointBackgroundColor: '#c9a74e',
        pointBorderColor: '#fff',
        pointBorderWidth: 2,
        pointRadius: 3,
        pointHoverRadius: 5,
      },
    ],
  }
})

const lineChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false },
    tooltip: {
      callbacks: {
        label: (ctx: any) => ` ₫${ctx.raw.toLocaleString('vi-VN')}`,
      },
    },
  },
  scales: {
    x: {
      grid: { display: false },
      ticks: { font: { size: 11 }, color: '#9ca3af' },
    },
    y: {
      grid: { color: 'rgba(0,0,0,0.05)' },
      ticks: {
        font: { size: 11 },
        color: '#9ca3af',
        callback: (v: any) => `₫${(v / 1000000).toFixed(1)}M`,
      },
    },
  },
}

const barChartData = computed(() => {
  if (!data.value) return { labels: [], datasets: [] }
  const movies = data.value.topMovies.map((m: TopMovieDto) =>
    m.movieTitle.length > 20 ? m.movieTitle.substring(0, 20) + '...' : m.movieTitle,
  )
  const counts = data.value.topMovies.map((m: TopMovieDto) => m.ticketCount)

  return {
    labels: movies,
    datasets: [
      {
        label: 'Số vé',
        data: counts,
        backgroundColor: [
          'rgba(124, 92, 255, 0.85)',
          'rgba(124, 92, 255, 0.7)',
          'rgba(124, 92, 255, 0.55)',
          'rgba(124, 92, 255, 0.4)',
          'rgba(124, 92, 255, 0.25)',
        ],
        borderRadius: 6,
        borderSkipped: false,
      },
    ],
  }
})

const barChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  indexAxis: 'x' as const,
  plugins: {
    legend: { display: false },
    tooltip: {
      callbacks: {
        label: (ctx: any) => ` ${ctx.raw} vé`,
      },
    },
  },
  scales: {
    x: {
      grid: { display: false },
      ticks: { font: { size: 11 }, color: '#6b7280' },
    },
    y: {
      grid: { color: 'rgba(0,0,0,0.05)' },
      ticks: {
        font: { size: 11 },
        color: '#9ca3af',
        stepSize: 1,
      },
    },
  },
}

const pieChartData = computed(() => {
  if (!data.value) return { labels: [], datasets: [] }
  return {
    labels: ['Vé', 'F&B'],
    datasets: [
      {
        data: [
          data.value.revenueStructure.ticketPercent,
          data.value.revenueStructure.fnbPercent,
        ],
        backgroundColor: ['#7c5cff', '#c9a74e'],
        borderWidth: 0,
        hoverOffset: 4,
      },
    ],
  }
})

const pieChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  cutout: '65%',
  plugins: {
    legend: { display: false },
    tooltip: {
      callbacks: {
        label: (ctx: any) => ` ${ctx.label}: ${ctx.raw}%`,
      },
    },
  },
}

// ── Mount ─────────────────────────────────────────────────────────────────────
onMounted(async () => {
  cinemas.value = await fetchAllCinemas()
  await fetchDashboard(null)
})
</script>
