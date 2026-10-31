/* Copyright 2009 by the Oxford University Computing Laboratory

   This file is part of HermiT.

   HermiT is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   HermiT is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with HermiT.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.semanticweb.HermiT.owl_wg_tests;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.params.provider.Arguments;
import org.semanticweb.HermiT.owl_wg_tests.WGTestDescriptor.TestType;

//OWL WG Tests without Status
class AllWGTestsWithoutStatus {
    public static List<Arguments> arguments(TestType testtype) throws Exception {
        List<Arguments> list=new ArrayList<>();
        WGTestRegistry wgTestRegistry = new WGTestRegistry();
        for (WGTestDescriptor wgTestDescriptor : wgTestRegistry.getTestDescriptors())
            if (wgTestDescriptor.isDLTest() && wgTestDescriptor.status == null)
                wgTestDescriptor.addTestsToSuite(testtype, list);
        return list;
    }
}
