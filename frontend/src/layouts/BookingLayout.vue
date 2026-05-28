<template>
    <div class="min-h-screen bg-bg-base text-text-primary">
        <div class="w-full bg-bg-surface border-b border-border-subtle px-4 py-3">
            <slot name="progress" />
        </div>

        <div class="flex flex-col lg:flex-row gap-12 p-4 lg:p-8 xl:p-16">
            <!-- Main content -->
            <div class="flex-1 min-w-0">
                <slot name="main" />
            </div>
            <!-- Sidebar -->
            <aside class="w-full lg:w-100 shrink-0">
                <div class="lg:sticky lg:top-6">
                    <slot name="sidebar" />
                </div>
            </aside>
        </div>
    </div>
</template>