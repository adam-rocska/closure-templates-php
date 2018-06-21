/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.template.soy.jssrc.internal;

import com.google.common.collect.ImmutableSet;
import com.google.template.soy.jssrc.restricted.JsExpr;
import com.google.template.soy.jssrc.restricted.SoyLibraryAssistedJsSrcFunction;
import java.util.List;
import java.util.Set;

/**
 * Soy special function for internal usages.
 *
 * <p>This function is explicitly not registered with {@link BasicFunctionsModule}. It exists for
 * client-side VE logging, and should not be used by Soy users.
 */
public final class VeLogJsSrcLoggingFunction implements SoyLibraryAssistedJsSrcFunction {
  // $$ prefix ensures that the function cannot be used directly
  public static final String NAME = "$$loggingFunction";

  public static final VeLogJsSrcLoggingFunction INSTANCE = new VeLogJsSrcLoggingFunction();

  // Do not @Inject; should not be used externally.
  private VeLogJsSrcLoggingFunction() {}

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public Set<Integer> getValidArgsSizes() {
    return ImmutableSet.of(4);
  }

  @Override
  public JsExpr computeForJsSrc(List<JsExpr> args) {
    return new JsExpr(
        String.format(
            "soy.velog.$$getLoggingFunctionAttribute(xid(%1$s), %2$s, %3$s, %4$s)",
            args.get(0).getText(),
            args.get(1).getText(),
            args.get(2).getText(),
            args.get(3).getText()),
        Integer.MAX_VALUE);
  }

  @Override
  public ImmutableSet<String> getRequiredJsLibNames() {
    return ImmutableSet.of("soy.velog", "xid");
  }
}
