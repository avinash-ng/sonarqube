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
import { sortBy } from 'lodash';
import * as React from 'react';
import { FormattedMessage } from 'react-intl';
import { createCondition, deleteCondition, updateCondition } from '../../../api/quality-gates';
import DocLink from '../../../components/common/DocLink';
import ConfirmModal from '../../../components/controls/ConfirmModal';
import { translate, translateWithParameters } from '../../../helpers/l10n';
import { Condition, Dict, Metric, QualityGate } from '../../../types/types';
import {
  getCaycConditionsWithCorrectValue,
  getCorrectCaycCondition,
  getWeakMissingAndNonCaycConditions,
} from '../utils';
import ConditionsTable from './ConditionsTable';

interface Props {
  canEdit: boolean;
  metrics: Dict<Metric>;
  updatedConditionId?: string;
  conditions: Condition[];
  scope: 'new' | 'overall' | 'new-cayc';
  onClose: () => void;
  onAddCondition: (condition: Condition) => void;
  onRemoveCondition: (condition: Condition) => void;
  onSaveCondition: (newCondition: Condition, oldCondition: Condition) => void;
  lockEditing: () => void;
  qualityGate: QualityGate;
}

export default class CaycReviewUpdateConditionsModal extends React.PureComponent<Props> {
  updateCaycQualityGate = () => {
    const { conditions, qualityGate } = this.props;
    const promiseArr: Promise<Condition | undefined | void>[] = [];
    const { weakConditions, missingConditions, nonCaycConditions } =
      getWeakMissingAndNonCaycConditions(conditions);

    weakConditions.forEach((condition) => {
      promiseArr.push(
        updateCondition({
          ...getCorrectCaycCondition(condition),
          id: condition.id,
        })
          .then((resultCondition) => {
            const currentCondition = conditions.find((con) => con.metric === condition.metric);
            if (currentCondition) {
              this.props.onSaveCondition(resultCondition, currentCondition);
            }
          })
          .catch(() => undefined)
      );
    });

    missingConditions.forEach((condition) => {
      promiseArr.push(
        createCondition({
          ...getCorrectCaycCondition(condition),
          gateId: qualityGate.id,
        })
          .then((resultCondition) => this.props.onAddCondition(resultCondition))
          .catch(() => undefined)
      );
    });

    nonCaycConditions.forEach((condition) => {
      promiseArr.push(
        deleteCondition({ id: condition.id })
          .then(() => this.props.onRemoveCondition(condition))
          .catch(() => undefined)
      );
    });

    return Promise.all(promiseArr).then(() => {
      this.props.lockEditing();
    });
  };

  render() {
    const { conditions, qualityGate, metrics } = this.props;
    const caycConditionsWithCorrectValue = getCaycConditionsWithCorrectValue(conditions);
    const sortedConditions = sortBy(
      caycConditionsWithCorrectValue,
      (condition) => metrics[condition.metric] && metrics[condition.metric].name
    );
    return (
      <ConfirmModal
        header={translateWithParameters(
          'quality_gates.cayc.review_update_modal.header',
          qualityGate.name
        )}
        confirmButtonText={translate('quality_gates.cayc.review_update_modal.confirm_text')}
        onClose={this.props.onClose}
        onConfirm={this.updateCaycQualityGate}
        size="medium"
      >
        <div className="quality-gate-section huge-spacer-bottom">
          <p>
            <FormattedMessage
              id="quality_gates.cayc.review_update_modal.description1"
              defaultMessage={translate('quality_gates.cayc.review_update_modal.description1')}
              values={{
                cayc_link: (
                  <DocLink to="/user-guide/clean-as-you-code/">
                    {translate('quality_gates.cayc')}
                  </DocLink>
                ),
              }}
            />
          </p>
          <p className="big-spacer-top big-spacer-bottom">
            {translate('quality_gates.cayc.review_update_modal.description2')}
          </p>
          <h3 className="medium text-normal spacer-top spacer-bottom">
            {translate('quality_gates.conditions.new_code', 'long')}
          </h3>
          <ConditionsTable
            {...this.props}
            conditions={sortedConditions}
            showEdit={false}
            isCaycModal={true}
          />
        </div>
      </ConfirmModal>
    );
  }
}
