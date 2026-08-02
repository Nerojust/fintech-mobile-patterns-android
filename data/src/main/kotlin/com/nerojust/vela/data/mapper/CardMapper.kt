package com.nerojust.vela.data.mapper

import com.nerojust.vela.core.network.dto.CardDto
import com.nerojust.vela.domain.model.Card
import com.nerojust.vela.domain.model.CardBrand

fun CardDto.toDomain(): Card =
    Card(
        id = id,
        brand = CardBrand.valueOf(brand),
        last4 = last4,
        expiryMonth = expiryMonth,
        expiryYear = expiryYear,
        isDefault = isDefault,
    )
