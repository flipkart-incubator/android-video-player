/*
 * Copyright (C) 2021 Flipkart Internet Pvt Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.flipkart.flick.core.db.repository

/**
 * Repository is nothing but an abstraction layer over the db and the network.
 * Whenever the observer asks the ViewModel to return the data, the ViewModel internally talks to the
 * repository for the data. Hence ViewModel is abstracted out from the understanding of how the data
 * is being fetched ie if it's being read from the db, or from the network.
 * <p>
 * The repository handles the logic of fetching the data from the network (in case of invalid data in db) and
 * updates the database.
 *
 * NOTE: This interface is empty for now, will add in methods when necessary
 */

interface Repository