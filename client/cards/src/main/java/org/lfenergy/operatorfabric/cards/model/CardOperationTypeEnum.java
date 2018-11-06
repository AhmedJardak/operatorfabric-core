/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.lfenergy.operatorfabric.cards.model;

/**
 * the severity of the card subject:
 * <dl>
 *     <dt>ADD</dt><dd>Operation lists cards object to add</dd>
 *     <dt>UPDATE</dt><dd>Operation lists cards object to update</dd>
 *     <dt>DELETE</dt><dd>Operation lists card ids to delete</dd>
 * </dl>
 *
 * @author David Binder
 */
public enum CardOperationTypeEnum {
  
  ADD,UPDATE,DELETE
}
