/*
 * SonarQube
 * Copyright (C) 2009-2023 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.server.platform.db.migration.step;

import java.util.List;
import org.sonar.db.dialect.Dialect;
import org.sonar.db.dialect.H2;
import org.sonar.db.dialect.MsSql;
import org.sonar.db.dialect.Oracle;
import org.sonar.db.dialect.PostgreSql;

import static java.util.Collections.singletonList;
import static org.sonar.server.platform.db.migration.def.Validations.validateIndexNameIgnoreCase;
import static org.sonar.server.platform.db.migration.def.Validations.validateTableName;

/**
 * Should not be used directly.
 * Use {@link org.sonar.server.platform.db.migration.step.DropIndexChange} instead.
 */
class DropIndexBuilder {

  private final Dialect dialect;
  private String tableName;
  private String indexName;

  DropIndexBuilder(Dialect dialect) {
    this.dialect = dialect;
  }

  public DropIndexBuilder setTable(String s) {
    this.tableName = s;
    return this;
  }

  public DropIndexBuilder setName(String s) {
    this.indexName = s;
    return this;
  }

  public List<String> build() {
    validateTableName(tableName);
    validateIndexNameIgnoreCase(indexName);
    return singletonList(createSqlStatement());
  }

  private String createSqlStatement() {
    return switch (dialect.getId()) {
      case MsSql.ID -> "DROP INDEX " + indexName + " ON " + tableName;
      case Oracle.ID -> "DROP INDEX " + indexName;
      case H2.ID, PostgreSql.ID -> "DROP INDEX IF EXISTS " + indexName;
      default -> throw new IllegalStateException("Unsupported dialect for drop of index: " + dialect);
    };
  }
}
