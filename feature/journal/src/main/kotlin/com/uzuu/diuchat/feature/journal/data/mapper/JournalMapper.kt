package com.uzuu.diuchat.feature.journal.data.mapper

import com.uzuu.diuchat.feature.journal.data.datasource.api.model.FavouriteMemoryDto
import com.uzuu.diuchat.feature.journal.data.datasource.api.model.MoodCheckinDto
import com.uzuu.diuchat.feature.journal.domain.model.DailyMoodCheckin
import com.uzuu.diuchat.feature.journal.domain.model.FavouriteMemory
import com.uzuu.diuchat.feature.journal.domain.model.JournalMoodType

class JournalMapper {
    fun toDomain(dto: MoodCheckinDto): DailyMoodCheckin = DailyMoodCheckin(
        dateIso = dto.dateIso,
        dayNumber = dto.dayNumber,
        mood = JournalMoodType.fromId(dto.moodId),
        privateNote = dto.privateNote,
        sharedWithContactIds = dto.sharedWithContactIds,
        displayDate = dto.displayDate,
    )

    fun toDto(domain: DailyMoodCheckin): MoodCheckinDto = MoodCheckinDto(
        dateIso = domain.dateIso,
        dayNumber = domain.dayNumber,
        moodId = domain.mood.id,
        privateNote = domain.privateNote,
        sharedWithContactIds = domain.sharedWithContactIds,
        displayDate = domain.displayDate,
    )

    fun toDomainList(dtos: List<MoodCheckinDto>): List<DailyMoodCheckin> = dtos.map(::toDomain)

    fun toDomain(dto: FavouriteMemoryDto): FavouriteMemory = FavouriteMemory(
        id = dto.id,
        senderName = dto.senderName,
        mood = JournalMoodType.fromId(dto.moodId),
        content = dto.content,
        savedDate = dto.savedDate,
        conversationId = dto.conversationId,
    )

    fun toDto(domain: FavouriteMemory): FavouriteMemoryDto = FavouriteMemoryDto(
        id = domain.id,
        senderName = domain.senderName,
        moodId = domain.mood.id,
        content = domain.content,
        savedDate = domain.savedDate,
        conversationId = domain.conversationId,
    )

    fun toFavouriteDomainList(dtos: List<FavouriteMemoryDto>): List<FavouriteMemory> = dtos.map(::toDomain)
}
