package org.koitharu.kotatsu.details.domain

import org.koitharu.kotatsu.core.parser.MangaRepository
import org.koitharu.kotatsu.core.util.ext.printStackTraceDebug
import org.koitharu.kotatsu.parsers.model.Manga
import org.koitharu.kotatsu.parsers.model.MangaListFilter
import org.koitharu.kotatsu.parsers.model.SortOrder
import org.koitharu.kotatsu.parsers.util.runCatchingCancellable
import javax.inject.Inject

private const val MAX_RELATED_RESULTS = 24

class RelatedMangaUseCase @Inject constructor(
	private val mangaRepositoryFactory: MangaRepository.Factory,
) {

	suspend operator fun invoke(seed: Manga): List<Manga>? {
		val repository = mangaRepositoryFactory.create(seed.source)

		// Try native getRelated first
		val nativeResult = runCatchingCancellable {
			repository.getRelated(seed)
		}.onFailure {
			it.printStackTraceDebug()
		}.getOrNull()

		if (!nativeResult.isNullOrEmpty()) {
			return nativeResult
		}

		// Fallback: search by tags
		val tagFallback = runCatchingCancellable {
			searchByTags(repository, seed)
		}.onFailure {
			it.printStackTraceDebug()
		}.getOrNull()

		if (!tagFallback.isNullOrEmpty()) {
			return tagFallback
		}

		// Fallback: search by title
		return runCatchingCancellable {
			searchByTitle(repository, seed)
		}.onFailure {
			it.printStackTraceDebug()
		}.getOrNull()
	}

	private suspend fun searchByTags(repository: MangaRepository, seed: Manga): List<Manga>? {
		val tags = seed.tags
		if (tags.isEmpty()) {
			return null
		}
		val sortOrder = if (SortOrder.POPULARITY in repository.sortOrders) {
			SortOrder.POPULARITY
		} else {
			repository.defaultSortOrder
		}
		val filter = MangaListFilter(tags = tags)
		val list = repository.getList(0, sortOrder, filter)
		return list
			.filter { it.id != seed.id }
			.take(MAX_RELATED_RESULTS)
			.ifEmpty { null }
	}

	private suspend fun searchByTitle(repository: MangaRepository, seed: Manga): List<Manga>? {
		if (seed.title.isBlank()) {
			return null
		}
		val sortOrder = if (SortOrder.RELEVANCE in repository.sortOrders) {
			SortOrder.RELEVANCE
		} else {
			repository.defaultSortOrder
		}
		val filter = MangaListFilter(query = seed.title)
		val list = repository.getList(0, sortOrder, filter)
		return list
			.filter { it.id != seed.id }
			.take(MAX_RELATED_RESULTS)
			.ifEmpty { null }
	}
}
