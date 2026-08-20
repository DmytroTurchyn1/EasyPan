package com.cook.easypan.easypan.data.repository

import android.content.Context
import android.util.Log
import com.cook.easypan.app.dataStore
import com.cook.easypan.easypan.data.mappers.toMealPlan
import com.cook.easypan.easypan.data.mappers.toMealPlanDto
import com.cook.easypan.easypan.domain.model.MealPlan
import com.cook.easypan.easypan.domain.repository.MealPlanRepository
import com.cook.easypan.easypan.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class DefaultMealPlanRepository(
    private val context: Context,
    private val recipeRepository: RecipeRepository,
) : MealPlanRepository {

    override suspend fun getSavedPlan(): MealPlan? {
        val dto = context.dataStore.data.first().savedMealPlan ?: return null

        val plan = dto.toMealPlan(recipeRepository.getRecipes())
        if (plan == null) {
            Log.d(TAG, "Saved plan could not be rebuilt from the catalog, discarding")
            clearPlan()
            return null
        }

        if (!plan.startDate.plusDays(DAYS_IN_PLAN).isAfter(LocalDate.now())) {
            Log.d(TAG, "Saved plan week has elapsed, discarding")
            clearPlan()
            return null
        }

        return plan
    }

    override suspend fun savePlan(plan: MealPlan) {
        context.dataStore.updateData { settings ->
            settings.copy(savedMealPlan = plan.toMealPlanDto())
        }
    }

    override suspend fun clearPlan() {
        context.dataStore.updateData { settings ->
            settings.copy(savedMealPlan = null)
        }
    }

    private companion object {
        const val TAG = "DefaultMealPlanRepo"
        const val DAYS_IN_PLAN = 7L
    }
}
