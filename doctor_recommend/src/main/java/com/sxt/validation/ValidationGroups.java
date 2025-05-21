package com.sxt.validation;

import jakarta.validation.groups.Default;

/**
 * Validation groups interface.
 */
public interface ValidationGroups {

    /**
     * Validation group for creation operations.
     */
    interface OnCreate extends Default {};

    /**
     * Validation group for update operations.
     */
    interface OnUpdate extends Default {};

} 