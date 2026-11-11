/**
 * This file is part of EWItool.
 *
 * <p>EWItool is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * <p>EWItool is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * <p>You should have received a copy of the GNU General Public License along with EWItool. If not,
 * see <http://www.gnu.org/licenses/>.
 */
package com.github.ledhed2222.ewitool;

/**
 * @author steve
 */
public class Debugger {

  // FIXME: Always set verbose to false before releasing project!
  public static final boolean VERBOSE = false;

  public static void log(Object o) {
    if (VERBOSE) System.out.println(o.toString());
  }
}
