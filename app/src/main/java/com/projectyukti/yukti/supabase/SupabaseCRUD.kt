package com.projectyukti.yukti.supabase

import com.projectyukti.yukti.gitignore.Constants
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from

class SupabaseCRUD {
    val supabase = createSupabaseClient(
        supabaseUrl = Constants().supabaseUrl,
        supabaseKey = Constants().supabaseApiKey
    ) {
        install(Postgrest)
    }
    suspend fun insertBusinessData(businessId: String?, businessName: String, itemName: String, addedOn: String, addedBy: String) {
        val data = mapOf(
            "business_id" to businessId,
            "business_name" to businessName,
            "item_name" to itemName,
            "added_on" to addedOn,
            "added_by" to addedBy
        )

        val result = supabase.from("Business_data").insert(data)
        // Handle the result or any errors here
    }
}