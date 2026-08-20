package com.cook.easypan.easypan.domain.repository

import com.cook.easypan.easypan.domain.model.MealPlan

interface MealPlanRepository {
    suspend fun getSavedPlan(): MealPlan?
    suspend fun savePlan(plan: MealPlan)
    suspend fun clearPlan()
}
