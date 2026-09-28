package io.github.eliangilsierra.taskflow.shared.pagination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class PageResultTest {

  @Test
  void computesTotalPagesRoundingUp() {
    PageResult<String> page = new PageResult<>(List.of("a"), 0, 10, 21);

    assertThat(page.totalPages()).isEqualTo(3);
  }

  @Test
  void mapsItemsKeepingPagingMetadata() {
    PageResult<Integer> page = new PageResult<>(List.of("a", "bb"), 1, 2, 5).map(String::length);

    assertThat(page.items()).containsExactly(1, 2);
    assertThat(page.page()).isEqualTo(1);
    assertThat(page.totalItems()).isEqualTo(5);
  }

  @Test
  void rejectsInvalidPageQueries() {
    assertThatThrownBy(() -> new PageQuery(-1, 10)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new PageQuery(0, 0)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new PageQuery(0, PageQuery.MAX_SIZE + 1))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
