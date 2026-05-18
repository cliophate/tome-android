package org.cliophate.tome.common

import androidx.paging.PagingSource

abstract class LibraryPagingSource<T : Any>(
  protected val onTotalCountChanged: (Int) -> Unit,
) : PagingSource<Int, T>()
