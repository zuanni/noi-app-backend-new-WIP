/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */

package org.dpppt.backend.sdk.data;

public interface EtagGeneratorInterface {

    /**
     * Generates etag from primary key
     *
     * @param primaryKey the primary key
     * @param contentType TODO
     * @return the etag
     */
    String getEtag(int primaryKey, String contentType);
}