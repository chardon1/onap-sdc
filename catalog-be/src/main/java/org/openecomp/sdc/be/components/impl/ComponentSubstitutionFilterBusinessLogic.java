/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.components.impl;

import static org.openecomp.sdc.be.dao.api.ActionStatus.SUBSTITUTION_FILTER_NOT_FOUND;
import static org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR;

import fj.data.Either;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.openecomp.sdc.be.components.impl.exceptions.BusinessLogicException;
import org.openecomp.sdc.be.components.impl.utils.NodeFilterConstraintAction;
import org.openecomp.sdc.be.components.validation.NodeFilterValidator;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.RequirementSubstitutionFilterPropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SubstitutionFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ArtifactsOperations;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.InterfaceOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.SubstitutionFilterOperation;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupTypeOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Component("componentSubstitutionFilterBusinessLogic")
public class ComponentSubstitutionFilterBusinessLogic extends BaseBusinessLogic {

    private static final Logger LOGGER = Logger.getLogger(ComponentSubstitutionFilterBusinessLogic.class);

    private final SubstitutionFilterOperation substitutionFilterOperation;
    private final NodeFilterValidator nodeFilterValidator;

    @Autowired
    public ComponentSubstitutionFilterBusinessLogic(final IElementOperation elementDao,
                                                    final IGroupOperation groupOperation,
                                                    final IGroupInstanceOperation groupInstanceOperation,
                                                    final IGroupTypeOperation groupTypeOperation,
                                                    final InterfaceOperation interfaceOperation,
                                                    final InterfaceLifecycleOperation interfaceLifecycleTypeOperation,
                                                    final ArtifactsOperations artifactToscaOperation,
                                                    final SubstitutionFilterOperation substitutionFilterOperation,
                                                    final NodeFilterValidator nodeFilterValidator) {
        super(elementDao, groupOperation, groupInstanceOperation, groupTypeOperation, interfaceOperation,
            interfaceLifecycleTypeOperation, artifactToscaOperation);
        this.substitutionFilterOperation = substitutionFilterOperation;
        this.nodeFilterValidator = nodeFilterValidator;
    }

    public Optional<SubstitutionFilterDataDefinition> createSubstitutionFilterIfNotExist(final String componentId,
                                                                                         final String componentInstanceId,
                                                                                         final boolean shouldLock,
                                                                                         final ComponentTypeEnum componentTypeEnum)
        throws BusinessLogicException {

        final Component component = getComponent(componentId);
        final Optional<ComponentInstance> componentInstanceOptional =
            getComponentInstance(componentInstanceId, component);

        Optional<SubstitutionFilterDataDefinition> substitutionFilterDataDefinition;
        if (componentInstanceOptional.isPresent()) {
            substitutionFilterDataDefinition = getSubstitutionFilterDataDefinition(componentInstanceOptional.get());
            if (substitutionFilterDataDefinition.isPresent()) {
                return substitutionFilterDataDefinition;
            }
        }
        boolean wasLocked = false;
        try {
            if (shouldLock) {
                lockComponent(component.getUniqueId(), component, "Create Substitution Filter on component");
                wasLocked = true;
            }
            final Either<SubstitutionFilterDataDefinition, StorageOperationStatus> result = substitutionFilterOperation
                .createSubstitutionFilter(componentId, componentInstanceId);
            if (result.isRight()) {
                janusGraphDao.rollback();
                LOGGER.error(BUSINESS_PROCESS_ERROR,
                    "Failed to Create Substitution filter on component with id {}", componentId);
                throw new BusinessLogicException(componentsUtils.getResponseFormatByResource(componentsUtils
                    .convertFromStorageResponse(result.right().value()), component.getSystemName()));
            }
            substitutionFilterDataDefinition = Optional.ofNullable(result.left().value());
            if (componentInstanceOptional.isPresent() && substitutionFilterDataDefinition.isPresent()) {
                componentInstanceOptional.get().setSubstitutionFilter(substitutionFilterDataDefinition.get());
            }
            janusGraphDao.commit();
            LOGGER.debug("Substitution filter successfully created in component {} . ", component.getSystemName());
        } catch (final Exception e) {
            janusGraphDao.rollback();
            LOGGER.error(BUSINESS_PROCESS_ERROR,
                "Exception occurred during add Component Substitution filter property values: {}", e.getMessage(), e);
            throw new BusinessLogicException(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));

        } finally {
            if (wasLocked) {
                unlockComponent(component.getUniqueId(), componentTypeEnum);
            }
        }

        return substitutionFilterDataDefinition;
    }

    public Optional<SubstitutionFilterDataDefinition> addSubstitutionFilter(final String componentId,
                                                                            final String componentInstanceId,
                                                                            final NodeFilterConstraintAction action,
                                                                            final String propertyName,
                                                                            final String constraint,
                                                                            final boolean shouldLock,
                                                                            final ComponentTypeEnum componentTypeEnum)
        throws BusinessLogicException {

        final Component component = getComponent(componentId);
        SubstitutionFilterDataDefinition substitutionFilterDataDefinition = validateAndReturnSubstitutionFilterDefinition(
            componentInstanceId,
            action, constraint, component);
        boolean wasLocked = false;
        try {
            if (shouldLock) {
                lockComponent(component.getUniqueId(), component, "Add Substitution Filter on Component");
                wasLocked = true;
            }
            final RequirementSubstitutionFilterPropertyDataDefinition newProperty =
                new RequirementSubstitutionFilterPropertyDataDefinition();
            newProperty.setName(propertyName);
            newProperty.setConstraints(Collections.singletonList(constraint));
            final Either<SubstitutionFilterDataDefinition, StorageOperationStatus> result = substitutionFilterOperation
                .addNewProperty(componentId, componentInstanceId, substitutionFilterDataDefinition, newProperty);

            if (result.isRight()) {
                janusGraphDao.rollback();
                throw new BusinessLogicException(componentsUtils.getResponseFormatByResource(componentsUtils
                    .convertFromStorageResponse(result.right().value()), component.getSystemName()));
            } else {
                substitutionFilterDataDefinition = result.left().value();
            }
            janusGraphDao.commit();
            LOGGER.debug("Substitution filter successfully created in component {} . ", component.getSystemName());

        } catch (final Exception e) {
            janusGraphDao.rollback();
            LOGGER.error(BUSINESS_PROCESS_ERROR,
                "Exception occurred during add component substitution filter property values: {}", e.getMessage(), e);
            throw new BusinessLogicException(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));

        } finally {
            if (wasLocked) {
                unlockComponent(component.getUniqueId(), componentTypeEnum);
            }
        }
        return Optional.ofNullable(substitutionFilterDataDefinition);
    }

    public Optional<SubstitutionFilterDataDefinition> updateSubstitutionFilter(final String componentId,
                                                                               final String componentInstanceId,
                                                                               final List<String> constraints,
                                                                               final boolean shouldLock,
                                                                               final ComponentTypeEnum componentTypeEnum)
        throws BusinessLogicException {

        final Component component = getComponent(componentId);

        final Either<Boolean, ResponseFormat> response = nodeFilterValidator
            .validateFilter(component, componentInstanceId, constraints, NodeFilterConstraintAction.UPDATE);
        if (response.isRight()) {
            throw new BusinessLogicException(componentsUtils
                .getResponseFormat(SUBSTITUTION_FILTER_NOT_FOUND, response.right().value().getFormattedMessage()));
        }
        final Optional<ComponentInstance> componentInstance = getComponentInstance(componentInstanceId,
            component);
        if (!componentInstance.isPresent()) {
            throw new BusinessLogicException(ResponseFormatManager.getInstance()
                .getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
        SubstitutionFilterDataDefinition substitutionFilterDataDefinition = componentInstance.get()
            .getSubstitutionFilter();
        if (substitutionFilterDataDefinition == null) {
            throw new BusinessLogicException(componentsUtils.getResponseFormat(SUBSTITUTION_FILTER_NOT_FOUND));
        }
        boolean wasLocked = false;
        try {
            if (shouldLock) {
                lockComponent(component.getUniqueId(), component, "Update Substitution Filter on Component");
                wasLocked = true;
            }
            final List<RequirementSubstitutionFilterPropertyDataDefinition> properties = constraints.stream()
                .map(this::getRequirementSubstitutionFilterPropertyDataDefinition).collect(Collectors.toList());
            final Either<SubstitutionFilterDataDefinition, StorageOperationStatus> result = substitutionFilterOperation
                .updateSubstitutionFilter(componentId, componentInstanceId, substitutionFilterDataDefinition, properties);

            if (result.isRight()) {
                janusGraphDao.rollback();
                throw new BusinessLogicException(componentsUtils.getResponseFormatByResource(componentsUtils
                    .convertFromStorageResponse(result.right().value()), component.getSystemName()));
            } else {
                substitutionFilterDataDefinition = result.left().value();
            }
            janusGraphDao.commit();
            LOGGER.debug("Substitution filter successfully updated in component {} . ", component.getSystemName());

        } catch (final Exception e) {
            janusGraphDao.rollback();
            LOGGER.error(BUSINESS_PROCESS_ERROR, this.getClass().getName(),
                "Exception occurred during update component substitution filter property values: {}", e);
            throw new BusinessLogicException(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));

        } finally {
            if (wasLocked) {
                unlockComponent(component.getUniqueId(), componentTypeEnum);
            }
        }
        return Optional.ofNullable(substitutionFilterDataDefinition);
    }

    public Optional<SubstitutionFilterDataDefinition> deleteSubstitutionFilter(final String componentId,
                                                                               final String componentInstanceId,
                                                                               final NodeFilterConstraintAction action,
                                                                               final String constraint,
                                                                               final int position,
                                                                               final boolean shouldLock,
                                                                               final ComponentTypeEnum componentTypeEnum)
        throws BusinessLogicException {

        final Component component = getComponent(componentId);
        SubstitutionFilterDataDefinition substitutionFilterDataDefinition =
            validateAndReturnSubstitutionFilterDefinition(componentInstanceId, action, constraint, component);
        boolean wasLocked = false;
        try {
            if (shouldLock) {
                lockComponent(component.getUniqueId(), component,"Add Node Filter on Component");
                wasLocked = true;
            }
            final Either<SubstitutionFilterDataDefinition, StorageOperationStatus> result = substitutionFilterOperation
                .deleteConstraint(componentId, componentInstanceId, substitutionFilterDataDefinition, position);
            if (result.isRight()) {
                janusGraphDao.rollback();
                throw new BusinessLogicException(componentsUtils.getResponseFormatByResource(componentsUtils
                    .convertFromStorageResponse(result.right().value()), component.getSystemName()));
            } else {
                substitutionFilterDataDefinition = result.left().value();
            }
            janusGraphDao.commit();
            LOGGER.debug("Substitution filter successfully deleted in component {} . ", component.getSystemName());

        } catch (final Exception e) {
            janusGraphDao.rollback();
            LOGGER.error(BUSINESS_PROCESS_ERROR,
                "Exception occurred during delete component substitution filter property values: {}",
                e.getMessage(), e);
            throw new BusinessLogicException(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));

        } finally {
            if (wasLocked) {
                unlockComponent(component.getUniqueId(), componentTypeEnum);
            }
        }
        return Optional.ofNullable(substitutionFilterDataDefinition);
    }

    private Optional<SubstitutionFilterDataDefinition> getSubstitutionFilterDataDefinition(
        final ComponentInstance componentInstance) {

        final SubstitutionFilterDataDefinition substitutionFilterDataDefinition =
            componentInstance.getSubstitutionFilter();
        if (componentInstance.getSubstitutionFilter() != null) {
            return Optional.ofNullable(substitutionFilterDataDefinition);
        }
        return Optional.empty();
    }

    private void unlockComponent(final String componentUniqueId,
                                 final ComponentTypeEnum componentType) {
        graphLockOperation.unlockComponent(componentUniqueId, componentType.getNodeType());
    }

    public User validateUser(final String userId) {
        final User user = userValidations.validateUserExists(userId);
        userValidations.validateUserRole(user, Arrays.asList(Role.DESIGNER, Role.ADMIN));
        return user;
    }

    private Optional<ComponentInstance> getComponentInstance(final String componentInstanceId,
                                                             final Component component) {
        return component.getComponentInstanceById(componentInstanceId);
    }

    private Optional<SubstitutionFilterDataDefinition> getComponentInstanceSubstitutionFilterDataDefinition(
        final String componentInstanceId, final Component component)
        throws BusinessLogicException {

        if (nodeFilterValidator.validateComponentInstanceExist(component, componentInstanceId).isRight()) {
            throw new BusinessLogicException(componentsUtils
                .getResponseFormat(SUBSTITUTION_FILTER_NOT_FOUND));
        }
        return getComponentInstance(componentInstanceId, component).map(ComponentInstance::getSubstitutionFilter);
    }

    private SubstitutionFilterDataDefinition validateAndReturnSubstitutionFilterDefinition(
        final String componentInstanceId, final NodeFilterConstraintAction action, final String constraint,
        final Component component) throws BusinessLogicException {

        validateSubstitutionFilter(component, componentInstanceId, action, constraint);
        final Optional<SubstitutionFilterDataDefinition> substitutionFilterDataDefinition =
            getComponentInstanceSubstitutionFilterDataDefinition(componentInstanceId, component);
        if (!substitutionFilterDataDefinition.isPresent()) {
            throw new BusinessLogicException(componentsUtils.getResponseFormat(SUBSTITUTION_FILTER_NOT_FOUND));
        }
        return substitutionFilterDataDefinition.get();
    }

    private void validateSubstitutionFilter(final Component component,
                                            final String componentInstanceId,
                                            final NodeFilterConstraintAction action,
                                            final String constraint) throws BusinessLogicException {
        final Either<Boolean, ResponseFormat> response = nodeFilterValidator
            .validateFilter(component, componentInstanceId, Collections.singletonList(constraint), action);
        if (response.isRight()) {
            throw new BusinessLogicException(componentsUtils
                .getResponseFormat(SUBSTITUTION_FILTER_NOT_FOUND, response.right().value().getFormattedMessage()));
        }
    }

    private RequirementSubstitutionFilterPropertyDataDefinition getRequirementSubstitutionFilterPropertyDataDefinition(
        final String constraint) {

        final RequirementSubstitutionFilterPropertyDataDefinition requirementSubstitutionFilterPropertyDataDefinition =
            new RequirementSubstitutionFilterPropertyDataDefinition();
        requirementSubstitutionFilterPropertyDataDefinition.setConstraints(Arrays.asList(constraint));
        return requirementSubstitutionFilterPropertyDataDefinition;
    }
}