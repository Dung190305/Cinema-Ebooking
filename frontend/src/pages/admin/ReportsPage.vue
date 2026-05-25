<template>
  <div class="flex h-full min-h-screen bg-slate-50">
    <!-- Sidebar Tabs (desktop) -->
    <aside class="hidden w-52 shrink-0 border-r border-slate-200 bg-white xl:flex xl:flex-col">
      <div class="border-b border-slate-100 px-5 py-5">
        <p class="text-[10px] font-semibold uppercase tracking-widest text-slate-400">Analytics</p>
        <h1 class="mt-1 text-sm font-bold text-slate-800">Báo cáo</h1>
      </div>
      <nav class="flex flex-col gap-0.5 p-2 pt-3">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          class="flex items-center gap-3 rounded-lg px-3 py-2.5 text-left text-sm transition"
          :class="
            activeTab === tab.key
              ? 'bg-accent/10 font-semibold text-accent'
              : 'font-medium text-slate-500 hover:bg-slate-50 hover:text-slate-800'
          "
          @click="activeTab = tab.key"
        >
          <component :is="tab.icon" class="size-4 shrink-0" />
          {{ tab.label }}
        </button>
      </nav>
    </aside>

    <!-- Main -->
    <div class="flex flex-1 flex-col overflow-hidden">
      <!-- Sticky top bar -->
      <header class="sticky top-0 z-10 border-b border-slate-200 bg-white shadow-sm">
        <!-- Mobile tabs -->
        <div class="flex gap-1 overflow-x-auto px-4 pt-3 pb-0 xl:hidden">
          <button
            v-for="tab in tabs"
            :key="tab.key"
            class="shrink-0 rounded-t-lg border-b-2 px-4 pb-2 text-xs font-medium transition"
            :class="
              activeTab === tab.key
                ? 'border-accent text-accent'
                : 'border-transparent text-slate-500 hover:text-slate-800'
            "
            @click="activeTab = tab.key"
          >
            {{ tab.label }}
          </button>
        </div>

        <!-- Filter bar -->
        <div class="flex flex-wrap items-center gap-2 px-5 py-3">
          <div
            class="flex items-center gap-1.5 rounded-lg border border-slate-200 bg-slate-50 px-3 py-2"
          >
            <CalendarDays class="size-3.5 shrink-0 text-slate-400" />
            <input
              v-model="fromDate"
              type="date"
              class="bg-transparent text-xs text-slate-700 outline-none"
            />
            <span class="text-slate-300">—</span>
            <input
              v-model="toDate"
              type="date"
              class="bg-transparent text-xs text-slate-700 outline-none"
            />
          </div>

          <select
            v-model="selectedCinemaId"
            class="rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-xs text-slate-700 outline-none transition focus:border-accent"
          >
            <option value="">Tất cả rạp</option>
            <option v-for="cinema in cinemas" :key="cinema.id" :value="String(cinema.id)">
              {{ getCinemaName(cinema) }}
            </option>
          </select>

          <select
            v-model="selectedMovieId"
            class="rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-xs text-slate-700 outline-none transition focus:border-accent"
          >
            <option value="">Tất cả phim</option>
            <option v-for="movie in movies" :key="movie.id" :value="String(movie.id)">
              {{ getMovieTitle(movie) }}
            </option>
          </select>

          <select
            v-if="activeTab === 'revenue'"
            v-model="groupBy"
            class="rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-xs text-slate-700 outline-none transition focus:border-accent"
          >
            <option value="DAY">Theo ngày</option>
            <option value="MONTH">Theo tháng</option>
            <option value="YEAR">Theo năm</option>
          </select>

          <div class="ml-auto flex items-center gap-2">
            <button
              class="inline-flex items-center gap-1.5 rounded-lg border border-slate-200 bg-white px-3 py-2 text-xs font-medium text-slate-600 transition hover:bg-slate-50"
              @click="resetFilters"
            >
              <RotateCcw class="size-3.5" />
              Đặt lại
            </button>
            <button
              class="inline-flex items-center gap-1.5 rounded-lg bg-accent px-3 py-2 text-xs font-semibold text-text-on-accent transition hover:opacity-90"
              @click="fetchReports"
            >
              <Search class="size-3.5" />
              Xem báo cáo
            </button>
          </div>
        </div>
      </header>

      <!-- Content -->
      <main class="flex-1 overflow-y-auto p-5">
        <!-- Error -->
        <div
          v-if="globalError"
          class="mb-4 rounded-lg border border-red-100 bg-red-50 px-4 py-3 text-xs text-red-600"
        >
          {{ globalError }}
        </div>

        <!-- Loading skeleton -->
        <div v-if="isLoading" class="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          <div v-for="i in 8" :key="i" class="h-24 animate-pulse rounded-xl bg-slate-200" />
        </div>

        <div v-else class="flex flex-col gap-5">
          <!-- TAB: OVERVIEW -->
          <section v-if="activeTab === 'overview'" class="flex flex-col gap-5">
            <div class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
              <StatCard
                title="Tổng doanh thu"
                :value="formatCurrency(overview.totalRevenue)"
                description="Từ booking đã thanh toán"
                icon="revenue"
                color="yellow"
              />
              <StatCard
                title="Vé đã bán"
                :value="formatNumber(overview.totalTicketsSold)"
                description="Tổng số vé đã thanh toán"
                icon="ticket"
                color="blue"
              />
              <StatCard
                title="Booking thành công"
                :value="formatNumber(overview.confirmedBookings)"
                description="Trạng thái CONFIRMED"
                icon="booking"
                color="green"
              />
              <StatCard
                title="TB / booking"
                :value="formatCurrency(overview.averageRevenuePerBooking)"
                description="Doanh thu trung bình mỗi đơn"
                icon="average"
                color="purple"
              />
            </div>

            <!-- Breakdown row -->
            <div class="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
              <div class="rounded-xl border border-slate-100 bg-white p-5 shadow-sm xl:col-span-1">
                <p class="mb-3 text-xs font-semibold uppercase tracking-wider text-slate-400">
                  Cơ cấu doanh thu
                </p>
                <RevenueBreakdownBar
                  :ticket="Number(overview.totalTicketRevenue)"
                  :combo="Number(overview.totalComboRevenue)"
                />
                <div class="mt-4 flex items-center justify-between text-xs">
                  <span class="flex items-center gap-1.5 text-slate-500">
                    <span class="inline-block size-2.5 rounded-full bg-accent" />
                    Vé: {{ formatCurrency(overview.totalTicketRevenue) }}
                  </span>
                  <span class="flex items-center gap-1.5 text-slate-500">
                    <span class="inline-block size-2.5 rounded-full bg-blue-400" />
                    Combo: {{ formatCurrency(overview.totalComboRevenue) }}
                  </span>
                </div>
              </div>

              <div class="rounded-xl border border-slate-100 bg-white p-5 shadow-sm">
                <p class="mb-3 text-xs font-semibold uppercase tracking-wider text-slate-400">
                  Tình trạng booking
                </p>
                <div class="flex flex-col gap-2">
                  <BookingStatusRow
                    label="Thành công"
                    :value="overview.confirmedBookings"
                    :total="overview.totalBookings"
                    color="bg-green-400"
                  />
                  <BookingStatusRow
                    label="Đang chờ"
                    :value="overview.pendingBookings"
                    :total="overview.totalBookings"
                    color="bg-yellow-400"
                  />
                  <BookingStatusRow
                    label="Đã hủy"
                    :value="overview.cancelledBookings"
                    :total="overview.totalBookings"
                    color="bg-red-400"
                  />
                </div>
                <p class="mt-3 text-right text-xs text-slate-400">
                  Tổng: {{ formatNumber(overview.totalBookings) }} booking
                </p>
              </div>

              <div class="rounded-xl border border-slate-100 bg-white p-5 shadow-sm">
                <p class="mb-3 text-xs font-semibold uppercase tracking-wider text-slate-400">
                  Tóm tắt nhanh
                </p>
                <div class="flex flex-col gap-3">
                  <QuickStat
                    label="Tổng doanh thu"
                    :value="formatCurrency(overview.totalRevenue)"
                  />
                  <QuickStat label="Tổng booking" :value="formatNumber(overview.totalBookings)" />
                  <QuickStat label="Vé đã bán" :value="formatNumber(overview.totalTicketsSold)" />
                </div>
              </div>
            </div>
          </section>

          <!-- TAB: REVENUE -->
          <section v-if="activeTab === 'revenue'" class="flex flex-col gap-5">
            <!-- Revenue trend full width -->
            <div class="rounded-xl border border-slate-100 bg-white p-5 shadow-sm">
              <div class="mb-4 flex items-center justify-between">
                <div>
                  <h2 class="text-sm font-semibold text-slate-800">Xu hướng doanh thu</h2>
                  <p class="text-xs text-slate-400">
                    Theo {{ groupByLabel }}, tính theo ngày thanh toán
                  </p>
                </div>
                <button
                  class="inline-flex items-center gap-1.5 rounded-lg border border-slate-200 px-3 py-1.5 text-xs font-medium text-slate-600 hover:bg-slate-50"
                  @click="exportRevenueTrend"
                >
                  <Download class="size-3.5" />Xuất CSV
                </button>
              </div>
              <div
                v-if="revenueTrend.length === 0"
                class="py-12 text-center text-sm text-slate-400"
              >
                Chưa có dữ liệu trong khoảng thời gian này
              </div>
              <div v-else class="flex flex-col gap-2.5">
                <div
                  v-for="item in revenueTrend"
                  :key="item.label"
                  class="grid items-center gap-3"
                  style="grid-template-columns: 110px 1fr 130px"
                >
                  <span class="truncate text-xs text-slate-500">{{ item.label }}</span>
                  <div class="h-2.5 w-full overflow-hidden rounded-full bg-slate-100">
                    <div
                      class="h-full rounded-full bg-accent transition-all duration-500"
                      :style="{ width: getRevenueBarWidth(item.revenue) }"
                    />
                  </div>
                  <span class="text-right text-xs font-semibold text-slate-700">{{
                    formatCurrency(item.revenue)
                  }}</span>
                </div>
              </div>
            </div>

            <!-- Payment methods + Financial summary -->
            <div class="grid gap-5 xl:grid-cols-2">
              <ReportTableCard
                title="Phương thức thanh toán"
                description="Tỷ trọng giao dịch theo ví/thanh toán"
                @export="exportPaymentMethods"
              >
                <table class="w-full text-xs">
                  <thead>
                    <tr class="border-b border-slate-100 bg-slate-50">
                      <th class="px-4 py-2.5 text-left font-semibold text-slate-500">
                        Phương thức
                      </th>
                      <th class="px-4 py-2.5 text-right font-semibold text-slate-500">Giao dịch</th>
                      <th class="px-4 py-2.5 text-right font-semibold text-slate-500">Tổng tiền</th>
                      <th class="px-4 py-2.5 text-right font-semibold text-slate-500">Tỷ lệ</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-if="paymentMethods.length === 0">
                      <td colspan="4" class="py-8 text-center text-slate-400">Chưa có dữ liệu</td>
                    </tr>
                    <tr
                      v-for="item in paymentMethods"
                      :key="item.method"
                      class="border-b border-slate-50 hover:bg-slate-50"
                    >
                      <td class="px-4 py-3 font-medium text-slate-700">{{ item.method }}</td>
                      <td class="px-4 py-3 text-right text-slate-600">
                        {{ formatNumber(item.transactionCount) }}
                      </td>
                      <td class="px-4 py-3 text-right font-semibold text-slate-800">
                        {{ formatCurrency(item.totalAmount) }}
                      </td>
                      <td class="px-4 py-3 text-right">
                        <span
                          class="rounded-full bg-accent/10 px-2 py-0.5 text-xs font-semibold text-accent"
                          >{{ formatPercent(item.percentage) }}</span
                        >
                      </td>
                    </tr>
                  </tbody>
                </table>
              </ReportTableCard>

              <div class="rounded-xl border border-slate-100 bg-white p-5 shadow-sm">
                <p class="mb-4 text-sm font-semibold text-slate-800">Tóm tắt tài chính</p>
                <div class="flex flex-col gap-3">
                  <div class="flex items-center justify-between rounded-lg bg-accent/5 px-4 py-3">
                    <span class="text-xs font-medium text-slate-600">Tổng doanh thu</span>
                    <span class="text-sm font-bold text-accent">{{
                      formatCurrency(overview.totalRevenue)
                    }}</span>
                  </div>
                  <div class="flex items-center justify-between px-1">
                    <span class="text-xs text-slate-500">Doanh thu vé</span>
                    <span class="text-xs font-semibold text-slate-700">{{
                      formatCurrency(overview.totalTicketRevenue)
                    }}</span>
                  </div>
                  <div class="flex items-center justify-between px-1">
                    <span class="text-xs text-slate-500">Doanh thu combo</span>
                    <span class="text-xs font-semibold text-slate-700">{{
                      formatCurrency(overview.totalComboRevenue)
                    }}</span>
                  </div>
                </div>
              </div>
            </div>
          </section>

          <!-- TAB: MOVIE & COMBO -->
          <section v-if="activeTab === 'movie-combo'" class="grid gap-5 xl:grid-cols-2">
            <ReportTableCard
              title="Hiệu suất phim"
              description="Doanh thu và vé bán theo từng phim"
              @export="exportMoviePerformance"
            >
              <table class="w-full text-xs">
                <thead>
                  <tr class="border-b border-slate-100 bg-slate-50">
                    <th class="px-4 py-2.5 text-left font-semibold text-slate-500">Phim</th>
                    <th class="px-4 py-2.5 text-right font-semibold text-slate-500">Booking</th>
                    <th class="px-4 py-2.5 text-right font-semibold text-slate-500">Vé</th>
                    <th class="px-4 py-2.5 text-right font-semibold text-slate-500">Doanh thu</th>
                    <th class="px-4 py-2.5 text-right font-semibold text-slate-500">Tỷ lệ</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="moviePerformance.length === 0">
                    <td colspan="5" class="py-8 text-center text-slate-400">Chưa có dữ liệu</td>
                  </tr>
                  <tr
                    v-for="item in moviePerformance"
                    :key="item.movieTitle"
                    class="border-b border-slate-50 hover:bg-slate-50"
                  >
                    <td class="max-w-[160px] truncate px-4 py-3 font-medium text-slate-700">
                      {{ item.movieTitle || '—' }}
                    </td>
                    <td class="px-4 py-3 text-right text-slate-600">
                      {{ formatNumber(item.bookingCount) }}
                    </td>
                    <td class="px-4 py-3 text-right text-slate-600">
                      {{ formatNumber(item.ticketSold) }}
                    </td>
                    <td class="px-4 py-3 text-right font-semibold text-slate-800">
                      {{ formatCurrency(item.revenue) }}
                    </td>
                    <td class="px-4 py-3 text-right">
                      <span
                        class="rounded-full bg-blue-50 px-2 py-0.5 text-xs font-semibold text-blue-600"
                        >{{ formatPercent(item.revenueShare) }}</span
                      >
                    </td>
                  </tr>
                </tbody>
              </table>
            </ReportTableCard>

            <ReportTableCard
              title="Combo / F&B bán chạy"
              description="Thống kê combo bắp nước đã bán"
              @export="exportComboSales"
            >
              <table class="w-full text-xs">
                <thead>
                  <tr class="border-b border-slate-100 bg-slate-50">
                    <th class="px-4 py-2.5 text-left font-semibold text-slate-500">Combo</th>
                    <th class="px-4 py-2.5 text-right font-semibold text-slate-500">Số lượng</th>
                    <th class="px-4 py-2.5 text-right font-semibold text-slate-500">Doanh thu</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="comboSales.length === 0">
                    <td colspan="3" class="py-8 text-center text-slate-400">Chưa có dữ liệu</td>
                  </tr>
                  <tr
                    v-for="item in comboSales"
                    :key="item.comboId ?? item.comboName"
                    class="border-b border-slate-50 hover:bg-slate-50"
                  >
                    <td class="px-4 py-3 font-medium text-slate-700">
                      {{ item.comboName || '—' }}
                    </td>
                    <td class="px-4 py-3 text-right text-slate-600">
                      {{ formatNumber(item.quantitySold) }}
                    </td>
                    <td class="px-4 py-3 text-right font-semibold text-slate-800">
                      {{ formatCurrency(item.totalRevenue) }}
                    </td>
                  </tr>
                </tbody>
              </table>
            </ReportTableCard>
          </section>

          <!-- TAB: CINEMA & ROOM -->
          <section v-if="activeTab === 'cinema-room'" class="flex flex-col gap-5">
            <div class="grid gap-5 xl:grid-cols-2">
              <ReportTableCard
                title="Hiệu suất rạp"
                description="Doanh thu và tỷ lệ lấp đầy theo rạp"
                @export="exportCinemaPerformance"
              >
                <table class="w-full text-xs">
                  <thead>
                    <tr class="border-b border-slate-100 bg-slate-50">
                      <th class="px-4 py-2.5 text-left font-semibold text-slate-500">Rạp</th>
                      <th class="px-4 py-2.5 text-right font-semibold text-slate-500">Vé</th>
                      <th class="px-4 py-2.5 text-right font-semibold text-slate-500">Doanh thu</th>
                      <th class="px-4 py-2.5 text-right font-semibold text-slate-500">Lấp đầy</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-if="cinemaPerformance.length === 0">
                      <td colspan="4" class="py-8 text-center text-slate-400">Chưa có dữ liệu</td>
                    </tr>
                    <tr
                      v-for="item in cinemaPerformance"
                      :key="item.cinemaId ?? item.cinemaName"
                      class="border-b border-slate-50 hover:bg-slate-50"
                    >
                      <td class="px-4 py-3 font-medium text-slate-700">
                        {{ item.cinemaName || '—' }}
                      </td>
                      <td class="px-4 py-3 text-right text-slate-600">
                        {{ formatNumber(item.ticketSold) }}
                      </td>
                      <td class="px-4 py-3 text-right font-semibold text-slate-800">
                        {{ formatCurrency(item.revenue) }}
                      </td>
                      <td class="px-4 py-3 text-right">
                        <OccupancyBadge :value="item.occupancyRate" />
                      </td>
                    </tr>
                  </tbody>
                </table>
              </ReportTableCard>

              <ReportTableCard
                title="Hiệu suất phòng chiếu"
                description="Vé bán, suất chiếu và lấp đầy theo phòng"
                @export="exportRoomPerformance"
              >
                <table class="w-full text-xs">
                  <thead>
                    <tr class="border-b border-slate-100 bg-slate-50">
                      <th class="px-4 py-2.5 text-left font-semibold text-slate-500">Phòng</th>
                      <th class="px-4 py-2.5 text-right font-semibold text-slate-500">Suất</th>
                      <th class="px-4 py-2.5 text-right font-semibold text-slate-500">Vé</th>
                      <th class="px-4 py-2.5 text-right font-semibold text-slate-500">Doanh thu</th>
                      <th class="px-4 py-2.5 text-right font-semibold text-slate-500">Lấp đầy</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-if="roomPerformance.length === 0">
                      <td colspan="5" class="py-8 text-center text-slate-400">Chưa có dữ liệu</td>
                    </tr>
                    <tr
                      v-for="item in roomPerformance"
                      :key="item.roomId ?? item.roomName"
                      class="border-b border-slate-50 hover:bg-slate-50"
                    >
                      <td class="px-4 py-3">
                        <p class="font-medium text-slate-700">{{ item.roomName || '—' }}</p>
                        <p class="text-slate-400">{{ item.cinemaName || '—' }}</p>
                      </td>
                      <td class="px-4 py-3 text-right text-slate-600">
                        {{ formatNumber(item.showtimeCount) }}
                      </td>
                      <td class="px-4 py-3 text-right text-slate-600">
                        {{ formatNumber(item.ticketSold) }}
                      </td>
                      <td class="px-4 py-3 text-right font-semibold text-slate-800">
                        {{ formatCurrency(item.revenue) }}
                      </td>
                      <td class="px-4 py-3 text-right">
                        <OccupancyBadge :value="item.occupancyRate" />
                      </td>
                    </tr>
                  </tbody>
                </table>
              </ReportTableCard>
            </div>

            <!-- Golden hours full width -->
            <ReportTableCard
              title="Khung giờ chiếu hiệu quả"
              description="Doanh thu và vé bán theo giờ bắt đầu suất chiếu"
              @export="exportGoldenHours"
            >
              <table class="w-full text-xs">
                <thead>
                  <tr class="border-b border-slate-100 bg-slate-50">
                    <th class="px-4 py-2.5 text-left font-semibold text-slate-500">Thứ</th>
                    <th class="px-4 py-2.5 text-right font-semibold text-slate-500">Giờ</th>
                    <th class="px-4 py-2.5 text-right font-semibold text-slate-500">Booking</th>
                    <th class="px-4 py-2.5 text-right font-semibold text-slate-500">Vé</th>
                    <th class="px-4 py-2.5 text-right font-semibold text-slate-500">Doanh thu</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="goldenHours.length === 0">
                    <td colspan="5" class="py-8 text-center text-slate-400">Chưa có dữ liệu</td>
                  </tr>
                  <tr
                    v-for="item in goldenHours"
                    :key="`${item.dayOfWeek}-${item.hour}`"
                    class="border-b border-slate-50 hover:bg-slate-50"
                  >
                    <td class="px-4 py-3 font-medium text-slate-700">
                      {{ dayOfWeekLabel(item.dayOfWeek) }}
                    </td>
                    <td class="px-4 py-3 text-right text-slate-600">{{ item.hour }}:00</td>
                    <td class="px-4 py-3 text-right text-slate-600">
                      {{ formatNumber(item.bookingCount) }}
                    </td>
                    <td class="px-4 py-3 text-right text-slate-600">
                      {{ formatNumber(item.ticketSold) }}
                    </td>
                    <td class="px-4 py-3 text-right font-semibold text-slate-800">
                      {{ formatCurrency(item.revenue) }}
                    </td>
                  </tr>
                </tbody>
              </table>
            </ReportTableCard>
          </section>

          <!-- TAB: CUSTOMER & PROMOTION -->
          <section v-if="activeTab === 'customer-promotion'" class="grid gap-5 xl:grid-cols-2">
            <div class="rounded-xl border border-slate-100 bg-white p-5 shadow-sm">
              <p class="text-sm font-semibold text-slate-800">Giữ chân khách hàng</p>
              <p class="mt-0.5 text-xs text-slate-400">Tần suất khách hàng quay lại rạp</p>

              <!-- Retention rate visual -->
              <div class="my-5 flex items-end gap-3">
                <div class="relative flex size-24 items-center justify-center">
                  <svg class="size-24 -rotate-90" viewBox="0 0 36 36">
                    <circle
                      cx="18"
                      cy="18"
                      r="15.9"
                      fill="none"
                      stroke="#f1f5f9"
                      stroke-width="3"
                    />
                    <circle
                      cx="18"
                      cy="18"
                      r="15.9"
                      fill="none"
                      stroke="var(--color-accent, #f59e0b)"
                      stroke-width="3"
                      stroke-dasharray="100"
                      :stroke-dashoffset="100 - Math.min(Number(retention.retentionRate || 0), 100)"
                      stroke-linecap="round"
                    />
                  </svg>
                  <span class="absolute text-sm font-bold text-slate-800">{{
                    formatPercent(retention.retentionRate)
                  }}</span>
                </div>
                <div class="flex flex-col gap-1">
                  <p class="text-xs text-slate-400">Tỷ lệ khách quay lại</p>
                  <p class="text-xs font-medium text-slate-600">
                    {{ formatNumber(retention.returningCustomers) }} /
                    {{ formatNumber(retention.totalCustomers) }} khách
                  </p>
                </div>
              </div>

              <div class="grid grid-cols-2 gap-3">
                <div class="rounded-lg bg-slate-50 px-4 py-3">
                  <p class="text-xs text-slate-400">Tổng khách hàng</p>
                  <p class="mt-0.5 text-sm font-bold text-slate-800">
                    {{ formatNumber(retention.totalCustomers) }}
                  </p>
                </div>
                <div class="rounded-lg bg-slate-50 px-4 py-3">
                  <p class="text-xs text-slate-400">Khách quay lại</p>
                  <p class="mt-0.5 text-sm font-bold text-green-600">
                    {{ formatNumber(retention.returningCustomers) }}
                  </p>
                </div>
                <div class="rounded-lg bg-slate-50 px-4 py-3">
                  <p class="text-xs text-slate-400">Mua 1 lần</p>
                  <p class="mt-0.5 text-sm font-bold text-slate-800">
                    {{ formatNumber(retention.oneTimeCustomers) }}
                  </p>
                </div>
                <div class="rounded-lg bg-slate-50 px-4 py-3">
                  <p class="text-xs text-slate-400">TB booking / khách</p>
                  <p class="mt-0.5 text-sm font-bold text-slate-800">
                    {{ formatNumber(retention.averageBookingsPerCustomer) }}
                  </p>
                </div>
              </div>
            </div>

            <ReportTableCard
              title="Hiệu quả mã khuyến mãi"
              description="Lượt dùng, tổng giảm và doanh thu từ coupon"
              @export="exportPromotionEffectiveness"
            >
              <table class="w-full text-xs">
                <thead>
                  <tr class="border-b border-slate-100 bg-slate-50">
                    <th class="px-4 py-2.5 text-left font-semibold text-slate-500">Coupon</th>
                    <th class="px-4 py-2.5 text-right font-semibold text-slate-500">Lượt dùng</th>
                    <th class="px-4 py-2.5 text-right font-semibold text-slate-500">Tổng giảm</th>
                    <th class="px-4 py-2.5 text-right font-semibold text-slate-500">Doanh thu</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="promotionEffectiveness.length === 0">
                    <td colspan="4" class="py-8 text-center text-slate-400">Chưa có dữ liệu</td>
                  </tr>
                  <tr
                    v-for="item in promotionEffectiveness"
                    :key="item.couponCode"
                    class="border-b border-slate-50 hover:bg-slate-50"
                  >
                    <td class="px-4 py-3 font-mono font-semibold text-slate-700">
                      {{ item.couponCode || '—' }}
                    </td>
                    <td class="px-4 py-3 text-right text-slate-600">
                      {{ formatNumber(item.usedCount) }}
                    </td>
                    <td class="px-4 py-3 text-right font-semibold text-red-500">
                      -{{ formatCurrency(item.totalDiscount) }}
                    </td>
                    <td class="px-4 py-3 text-right font-semibold text-slate-800">
                      {{ formatCurrency(item.revenueGenerated) }}
                    </td>
                  </tr>
                </tbody>
              </table>
            </ReportTableCard>
          </section>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, defineComponent, h, onMounted, ref, watch } from 'vue'
import type { Component } from 'vue'
import {
  BarChart3,
  CalendarDays,
  Download,
  LayoutDashboard,
  RefreshCw,
  RotateCcw,
  Search,
  Ticket,
  TrendingUp,
  Users,
  Wallet,
} from 'lucide-vue-next'
import apiClient from '@/api/axios'
import { movieApi } from '@/api/movie.api'
import { reportApi } from '@/api/report.api'
import type {
  CinemaPerformanceResponse,
  ComboSalesReportResponse,
  GoldenHourResponse,
  MoviePerformanceResponse,
  PaymentMethodReportResponse,
  PromotionEffectivenessResponse,
  ReportFilterParams,
  ReportGroupBy,
  RetentionReportResponse,
  RevenueOverviewResponse,
  RevenueTrendPointResponse,
  RoomPerformanceResponse,
} from '@/types/report.types'

type ReportTab = 'overview' | 'revenue' | 'movie-combo' | 'cinema-room' | 'customer-promotion'

interface MovieOption {
  id: number
  title?: string
  name?: string
}
interface CinemaOption {
  id: number
  name?: string
  cinemaName?: string
}
interface PageLike<T> {
  content?: T[]
}

const tabs: Array<{ key: ReportTab; label: string; icon: Component }> = [
  { key: 'overview', label: 'Tổng quan', icon: LayoutDashboard },
  { key: 'revenue', label: 'Doanh thu', icon: TrendingUp },
  { key: 'movie-combo', label: 'Phim & Combo', icon: Ticket },
  { key: 'cinema-room', label: 'Rạp & Phòng', icon: BarChart3 },
  { key: 'customer-promotion', label: 'KH & Khuyến mãi', icon: Users },
]

const emptyOverview: RevenueOverviewResponse = {
  totalRevenue: 0,
  totalTicketRevenue: 0,
  totalComboRevenue: 0,
  totalBookings: 0,
  confirmedBookings: 0,
  pendingBookings: 0,
  cancelledBookings: 0,
  totalTicketsSold: 0,
  averageRevenuePerBooking: 0,
}
const emptyRetention: RetentionReportResponse = {
  totalCustomers: 0,
  returningCustomers: 0,
  oneTimeCustomers: 0,
  retentionRate: 0,
  averageBookingsPerCustomer: 0,
}

const activeTab = ref<ReportTab>('overview')
const isLoading = ref(false)
const globalError = ref('')
const fromDate = ref('')
const toDate = ref('')
const selectedCinemaId = ref('')
const selectedMovieId = ref('')
const groupBy = ref<ReportGroupBy>('DAY')
const movies = ref<MovieOption[]>([])
const cinemas = ref<CinemaOption[]>([])
const overview = ref<RevenueOverviewResponse>({ ...emptyOverview })
const revenueTrend = ref<RevenueTrendPointResponse[]>([])
const moviePerformance = ref<MoviePerformanceResponse[]>([])
const comboSales = ref<ComboSalesReportResponse[]>([])
const paymentMethods = ref<PaymentMethodReportResponse[]>([])
const cinemaPerformance = ref<CinemaPerformanceResponse[]>([])
const roomPerformance = ref<RoomPerformanceResponse[]>([])
const goldenHours = ref<GoldenHourResponse[]>([])
const retention = ref<RetentionReportResponse>({ ...emptyRetention })
const promotionEffectiveness = ref<PromotionEffectivenessResponse[]>([])

const groupByLabel = computed(() => ({ MONTH: 'tháng', YEAR: 'năm', DAY: 'ngày' })[groupBy.value])
const maxRevenueTrend = computed(() =>
  Math.max(...revenueTrend.value.map((item) => Number(item.revenue || 0)), 0),
)

// ─── Inline components ────────────────────────────────────────────────────────

const StatCard = defineComponent({
  props: {
    title: { type: String, required: true },
    value: { type: String, required: true },
    description: { type: String, required: true },
    icon: { type: String, required: true },
    color: { type: String, default: 'yellow' },
  },
  setup(props) {
    const iconMap: Record<string, Component> = {
      revenue: Wallet,
      ticket: Ticket,
      booking: BarChart3,
      average: RefreshCw,
    }
    const colorMap: Record<string, string> = {
      yellow: 'bg-yellow-50 text-yellow-500',
      blue: 'bg-blue-50 text-blue-500',
      green: 'bg-green-50 text-green-500',
      purple: 'bg-purple-50 text-purple-500',
    }
    return () => {
      const Icon = iconMap[props.icon] || BarChart3
      return h('div', { class: 'rounded-xl border border-slate-100 bg-white p-5 shadow-sm' }, [
        h('div', { class: 'flex items-start justify-between gap-3' }, [
          h('div', null, [
            h('p', { class: 'text-xs text-slate-400' }, props.title),
            h('p', { class: 'mt-2 text-xl font-bold text-slate-900' }, props.value),
            h('p', { class: 'mt-1 text-xs text-slate-400' }, props.description),
          ]),
          h(
            'div',
            {
              class: `flex size-9 items-center justify-center rounded-lg ${colorMap[props.color] || colorMap.yellow}`,
            },
            [h(Icon, { class: 'size-4' })],
          ),
        ]),
      ])
    }
  },
})

const QuickStat = defineComponent({
  props: { label: { type: String, required: true }, value: { type: String, required: true } },
  setup(props) {
    return () =>
      h('div', { class: 'flex items-center justify-between' }, [
        h('span', { class: 'text-xs text-slate-500' }, props.label),
        h('span', { class: 'text-xs font-semibold text-slate-800' }, props.value),
      ])
  },
})

const RevenueBreakdownBar = defineComponent({
  props: { ticket: { type: Number, default: 0 }, combo: { type: Number, default: 0 } },
  setup(props) {
    return () => {
      const total = props.ticket + props.combo
      const ticketPct = total > 0 ? (props.ticket / total) * 100 : 50
      return h('div', { class: 'h-3 w-full overflow-hidden rounded-full bg-slate-100 flex' }, [
        h('div', {
          class: 'h-full bg-accent transition-all duration-500',
          style: { width: `${ticketPct}%` },
        }),
        h('div', { class: 'h-full bg-blue-400 transition-all duration-500 flex-1' }),
      ])
    }
  },
})

const BookingStatusRow = defineComponent({
  props: {
    label: { type: String, required: true },
    value: { type: Number, default: 0 },
    total: { type: Number, default: 0 },
    color: { type: String, default: 'bg-slate-300' },
  },
  setup(props) {
    return () => {
      const pct = props.total > 0 ? (props.value / props.total) * 100 : 0
      return h('div', { class: 'flex flex-col gap-1' }, [
        h('div', { class: 'flex items-center justify-between' }, [
          h('span', { class: 'text-xs text-slate-500' }, props.label),
          h('span', { class: 'text-xs font-semibold text-slate-700' }, String(props.value)),
        ]),
        h('div', { class: 'h-1.5 w-full overflow-hidden rounded-full bg-slate-100' }, [
          h('div', {
            class: `h-full rounded-full ${props.color} transition-all`,
            style: { width: `${pct}%` },
          }),
        ]),
      ])
    }
  },
})

const OccupancyBadge = defineComponent({
  props: { value: { type: Number, default: 0 } },
  setup(props) {
    return () => {
      const v = Number(props.value || 0)
      const cls =
        v >= 70
          ? 'bg-green-50 text-green-600'
          : v >= 40
            ? 'bg-yellow-50 text-yellow-600'
            : 'bg-red-50 text-red-500'
      return h(
        'span',
        { class: `rounded-full px-2 py-0.5 text-xs font-semibold ${cls}` },
        `${v.toFixed(1)}%`,
      )
    }
  },
})

const ReportTableCard = defineComponent({
  emits: ['export'],
  props: {
    title: { type: String, required: true },
    description: { type: String, required: true },
  },
  setup(props, { slots, emit }) {
    return () =>
      h(
        'section',
        { class: 'overflow-hidden rounded-xl border border-slate-100 bg-white shadow-sm' },
        [
          h(
            'div',
            { class: 'flex items-start justify-between gap-4 border-b border-slate-100 px-5 py-4' },
            [
              h('div', null, [
                h('h2', { class: 'text-sm font-semibold text-slate-800' }, props.title),
                h('p', { class: 'mt-0.5 text-xs text-slate-400' }, props.description),
              ]),
              h(
                'button',
                {
                  class:
                    'inline-flex items-center gap-1.5 rounded-lg border border-slate-200 px-2.5 py-1.5 text-xs font-medium text-slate-600 hover:bg-slate-50',
                  onClick: () => emit('export'),
                },
                [h(Download, { class: 'size-3.5' }), 'CSV'],
              ),
            ],
          ),
          h('div', { class: 'overflow-x-auto' }, slots.default?.()),
        ],
      )
  },
})

// ─── Helpers ──────────────────────────────────────────────────────────────────

function formatDateInput(date: Date) {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}
function setDefaultDateRange() {
  const current = new Date()
  fromDate.value = formatDateInput(new Date(current.getFullYear(), current.getMonth(), 1))
  toDate.value = formatDateInput(new Date(current.getFullYear(), current.getMonth() + 1, 0))
}
function getFilterParams(): ReportFilterParams {
  return {
    fromDate: fromDate.value,
    toDate: toDate.value,
    cinemaId: selectedCinemaId.value ? Number(selectedCinemaId.value) : '',
    movieId: selectedMovieId.value ? Number(selectedMovieId.value) : '',
  }
}
function getMovieTitle(movie: MovieOption) {
  return movie.title || movie.name || `Phim #${movie.id}`
}
function getCinemaName(cinema: CinemaOption) {
  return cinema.name || cinema.cinemaName || `Rạp #${cinema.id}`
}
function getRevenueBarWidth(value: number) {
  if (maxRevenueTrend.value <= 0) return '0%'
  return `${Math.max((Number(value || 0) / maxRevenueTrend.value) * 100, 3)}%`
}
function formatCurrency(value?: number | string | null) {
  return new Intl.NumberFormat('vi-VN').format(Number(value || 0)) + ' đ'
}
function formatNumber(value?: number | string | null) {
  return new Intl.NumberFormat('vi-VN').format(Number(value || 0))
}
function formatPercent(value?: number | string | null) {
  return `${Number(value || 0).toFixed(2)}%`
}
function dayOfWeekLabel(day: string) {
  return (
    {
      MONDAY: 'Thứ 2',
      TUESDAY: 'Thứ 3',
      WEDNESDAY: 'Thứ 4',
      THURSDAY: 'Thứ 5',
      FRIDAY: 'Thứ 6',
      SATURDAY: 'Thứ 7',
      SUNDAY: 'Chủ nhật',
      UNKNOWN: 'Không rõ',
    }[day] || day
  )
}
function getErrorMessage(error: unknown, fallback: string) {
  return error instanceof Error ? error.message : fallback
}

// ─── Data fetching ────────────────────────────────────────────────────────────

async function fetchMovies() {
  try {
    const res = (await movieApi.getList({
      page: 0,
      size: 100,
      status: 'NOW_SHOWING',
    })) as unknown as PageLike<MovieOption>
    movies.value = res.content || []
  } catch {
    movies.value = []
  }
}

async function fetchCinemas() {
  try {
    const res = (await apiClient.get<PageLike<CinemaOption> | CinemaOption[]>('/cinemas', {
      params: { page: 0, size: 100 },
    })) as unknown as PageLike<CinemaOption> | CinemaOption[]
    cinemas.value = Array.isArray(res) ? res : res.content || []
  } catch {
    cinemas.value = []
  }
}

async function fetchReports() {
  isLoading.value = true
  globalError.value = ''
  try {
    await fetchReportsByTab(activeTab.value)
  } catch (error: unknown) {
    globalError.value = getErrorMessage(
      error,
      'Ngày bắt đầu phải nhỏ hơn ngày kết thúc, giới hạn thống kê là 1 năm.',
    )
  } finally {
    isLoading.value = false
  }
}

async function fetchReportsByTab(tab: ReportTab) {
  const params = getFilterParams()
  if (tab === 'overview') {
    overview.value = (await reportApi.getOverview(params)) || { ...emptyOverview }
  } else if (tab === 'revenue') {
    const [trendRes, paymentRes, overviewRes] = await Promise.all([
      reportApi.getRevenueTrend({ ...params, groupBy: groupBy.value }),
      reportApi.getPaymentMethods(params),
      reportApi.getOverview(params),
    ])
    revenueTrend.value = trendRes || []
    paymentMethods.value = paymentRes || []
    overview.value = overviewRes || { ...emptyOverview }
  } else if (tab === 'movie-combo') {
    const [movieRes, comboRes] = await Promise.all([
      reportApi.getMoviePerformance(params),
      reportApi.getComboSales(params),
    ])
    moviePerformance.value = movieRes || []
    comboSales.value = comboRes || []
  } else if (tab === 'cinema-room') {
    const [cinemaRes, roomRes, goldenHourRes] = await Promise.all([
      reportApi.getCinemaPerformance(params),
      reportApi.getRoomPerformance(params),
      reportApi.getGoldenHours(params),
    ])
    cinemaPerformance.value = cinemaRes || []
    roomPerformance.value = roomRes || []
    goldenHours.value = goldenHourRes || []
  } else if (tab === 'customer-promotion') {
    const [retentionRes, promotionRes] = await Promise.all([
      reportApi.getRetention(params),
      reportApi.getPromotionEffectiveness(params),
    ])
    retention.value = retentionRes || { ...emptyRetention }
    promotionEffectiveness.value = promotionRes || []
  }
}

async function resetFilters() {
  selectedCinemaId.value = ''
  selectedMovieId.value = ''
  groupBy.value = 'DAY'
  setDefaultDateRange()
  await fetchReports()
}

// ─── CSV exports ──────────────────────────────────────────────────────────────

function escapeCsvValue(value: string | number | null | undefined) {
  return `"${String(value ?? '').replace(/"/g, '""')}"`
}
function exportCsv(
  filename: string,
  headers: string[],
  rows: Array<Array<string | number | null | undefined>>,
) {
  const content = [
    headers.map(escapeCsvValue).join(','),
    ...rows.map((r) => r.map(escapeCsvValue).join(',')),
  ].join('\n')
  const url = globalThis.URL.createObjectURL(
    new globalThis.Blob([`\uFEFF${content}`], { type: 'text/csv;charset=utf-8;' }),
  )
  const a = globalThis.document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
  globalThis.URL.revokeObjectURL(url)
}
function exportRevenueTrend() {
  exportCsv(
    'revenue-trend.csv',
    ['Thời gian', 'Doanh thu', 'Doanh thu vé', 'Doanh thu combo', 'Booking', 'Vé'],
    revenueTrend.value.map((i) => [
      i.label,
      i.revenue,
      i.ticketRevenue,
      i.comboRevenue,
      i.bookingCount,
      i.ticketCount,
    ]),
  )
}
function exportMoviePerformance() {
  exportCsv(
    'movie-performance.csv',
    ['Phim', 'Booking', 'Vé bán', 'Doanh thu', 'Tỷ lệ'],
    moviePerformance.value.map((i) => [
      i.movieTitle,
      i.bookingCount,
      i.ticketSold,
      i.revenue,
      i.revenueShare,
    ]),
  )
}
function exportComboSales() {
  exportCsv(
    'combo-sales.csv',
    ['Combo', 'Số lượng', 'Doanh thu'],
    comboSales.value.map((i) => [i.comboName, i.quantitySold, i.totalRevenue]),
  )
}
function exportPaymentMethods() {
  exportCsv(
    'payment-methods.csv',
    ['Phương thức', 'Giao dịch', 'Tổng tiền', 'Tỷ lệ'],
    paymentMethods.value.map((i) => [i.method, i.transactionCount, i.totalAmount, i.percentage]),
  )
}
function exportCinemaPerformance() {
  exportCsv(
    'cinema-performance.csv',
    ['Rạp', 'Booking', 'Vé', 'Doanh thu', 'Lấp đầy'],
    cinemaPerformance.value.map((i) => [
      i.cinemaName,
      i.bookingCount,
      i.ticketSold,
      i.revenue,
      i.occupancyRate,
    ]),
  )
}
function exportRoomPerformance() {
  exportCsv(
    'room-performance.csv',
    ['Rạp', 'Phòng', 'Booking', 'Vé', 'Suất', 'Doanh thu', 'Lấp đầy'],
    roomPerformance.value.map((i) => [
      i.cinemaName,
      i.roomName,
      i.bookingCount,
      i.ticketSold,
      i.showtimeCount,
      i.revenue,
      i.occupancyRate,
    ]),
  )
}
function exportGoldenHours() {
  exportCsv(
    'golden-hours.csv',
    ['Thứ', 'Giờ', 'Booking', 'Vé', 'Doanh thu'],
    goldenHours.value.map((i) => [
      dayOfWeekLabel(i.dayOfWeek),
      `${i.hour}:00`,
      i.bookingCount,
      i.ticketSold,
      i.revenue,
    ]),
  )
}
function exportPromotionEffectiveness() {
  exportCsv(
    'promotion-effectiveness.csv',
    ['Coupon', 'Lượt dùng', 'Tổng giảm', 'Doanh thu', 'TB giảm/booking'],
    promotionEffectiveness.value.map((i) => [
      i.couponCode,
      i.usedCount,
      i.totalDiscount,
      i.revenueGenerated,
      i.averageDiscountPerBooking,
    ]),
  )
}

watch(activeTab, () => fetchReports())
onMounted(async () => {
  setDefaultDateRange()
  await Promise.all([fetchMovies(), fetchCinemas()])
  await fetchReports()
})
</script>
