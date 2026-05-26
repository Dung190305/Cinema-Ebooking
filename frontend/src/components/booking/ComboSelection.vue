<script setup lang="ts">
import { ref, inject } from 'vue'
import { comboApi } from '@/api/combo.api'
import type { ComboResponse } from '@/types/combo.types'
import type { BookingComboInfo } from '@/types/booking.types'

const emit = defineEmits(['next', 'prev'])
const booking = inject<any>('booking')!
const combos = ref<ComboResponse[]>([])
const quantities = ref<Record<number, number>>({})

comboApi.getList(0, 50).then(res => combos.value = res.content.filter(c => c.displayStatus === 'ACTIVE'))

function addCombo(c: ComboResponse) {
    if (!quantities.value[c.id]) quantities.value[c.id] = 0
    quantities.value[c.id]++
    updateCombos()
}
function removeCombo(c: ComboResponse) {
    if (quantities.value[c.id] > 0) quantities.value[c.id]--
    updateCombos()
}

function updateCombos() {
    const list: BookingComboInfo[] = []
    for (const [id, qty] of Object.entries(quantities.value)) {
        if (qty > 0) {
            const combo = combos.value.find(c => c.id === Number(id))
            if (combo) list.push({ comboId: combo.id, comboName: combo.name, quantity: qty, unitPrice: combo.price, totalPrice: combo.price * qty })
        }
    }
    booking.selectedCombos.value = list
}

function validateAndNext() {
    emit('next')
    return true
}
defineExpose({ next: validateAndNext })
</script>

<template>
    <div>
        <div class="flex justify-between mb-4">
            <h2 class="text-title">Chọn bắp nước</h2>
        </div>
        <div class="grid grid-cols-1 gap-4">
            <div v-for="c in combos" :key="c.id"
                class="bg-bg-surface border border-border-default rounded-xl p-4 flex items-center gap-3">
                <img :src="c.imageUrl" class="w-16 h-16 object-cover rounded-lg" />
                <div class="flex-1">
                    <p class="text-body font-medium">{{ c.name }}</p>
                    <p class="text-caption text-text-secondary">{{ c.description }}</p>
                    <p class="text-accent font-semibold">{{ c.price.toLocaleString() }}đ</p>
                </div>
                <div class="flex items-center gap-2">
                    <button class="w-7 h-7 rounded border border-border-default text-text-secondary"
                        @click="removeCombo(c)">−</button>
                    <span class="w-6 text-center">{{ quantities[c.id] || 0 }}</span>
                    <button class="w-7 h-7 rounded border border-border-default text-text-secondary"
                        @click="addCombo(c)">+</button>
                </div>
            </div>
        </div>
    </div>
</template>