/*
 * Copyright 2009 Google Inc.
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

package com.google.template.soy.bidifunctions;

import com.google.common.collect.ImmutableSet;
import com.google.template.soy.data.SoyValue;
import com.google.template.soy.data.restricted.StringData;
import com.google.template.soy.exprtree.Operator;
import com.google.template.soy.internal.i18n.BidiGlobalDir;
import com.google.template.soy.jbcsrc.restricted.JbcSrcPluginContext;
import com.google.template.soy.jbcsrc.restricted.MethodRef;
import com.google.template.soy.jbcsrc.restricted.SoyExpression;
import com.google.template.soy.jbcsrc.restricted.SoyJbcSrcFunction;
import com.google.template.soy.jssrc.restricted.JsExpr;
import com.google.template.soy.jssrc.restricted.SoyLibraryAssistedJsSrcFunction;
import com.google.template.soy.pysrc.restricted.PyExpr;
import com.google.template.soy.pysrc.restricted.PyExprUtils;
import com.google.template.soy.pysrc.restricted.SoyPySrcFunction;
import com.google.template.soy.shared.restricted.Signature;
import com.google.template.soy.shared.restricted.SoyFunctionSignature;
import com.google.template.soy.shared.restricted.SoyJavaFunction;
import com.google.template.soy.shared.restricted.TypedSoyFunction;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Soy function that gets the name of the end edge ('left' or 'right') for the current global bidi
 * directionality.
 *
 */
@SoyFunctionSignature(name = "bidiEndEdge", value = @Signature(returnType = "string"))
@Singleton
final class BidiEndEdgeFunction extends TypedSoyFunction
    implements SoyJavaFunction,
        SoyLibraryAssistedJsSrcFunction,
        SoyPySrcFunction,
        SoyJbcSrcFunction {

  /** Provider for the current bidi global directionality. */
  private final Provider<BidiGlobalDir> bidiGlobalDirProvider;

  /** @param bidiGlobalDirProvider Provider for the current bidi global directionality. */
  @Inject
  BidiEndEdgeFunction(Provider<BidiGlobalDir> bidiGlobalDirProvider) {
    this.bidiGlobalDirProvider = bidiGlobalDirProvider;
  }

  @Override
  public SoyValue computeForJava(List<SoyValue> args) {
    return StringData.forValue(BidiFunctionsRuntime.bidiEndEdge(bidiGlobalDirProvider.get()));
  }

  // lazy singleton pattern, allows other backends to avoid the work.
  private static final class JbcSrcMethods {
    static final MethodRef END_EDGE =
        MethodRef.create(BidiFunctionsRuntime.class, "bidiEndEdge", BidiGlobalDir.class)
            .asCheap()
            .asNonNullable();
  }

  @Override
  public SoyExpression computeForJbcSrc(JbcSrcPluginContext context, List<SoyExpression> args) {
    return SoyExpression.forString(JbcSrcMethods.END_EDGE.invoke(context.getBidiGlobalDir()));
  }

  @Override
  public JsExpr computeForJsSrc(List<JsExpr> args) {
    BidiGlobalDir bidiGlobalDir = bidiGlobalDirProvider.get();
    if (bidiGlobalDir.isStaticValue()) {
      return new JsExpr(
          (bidiGlobalDir.getStaticValue() < 0) ? "'left'" : "'right'", Integer.MAX_VALUE);
    }
    return new JsExpr(
        "(" + bidiGlobalDir.getCodeSnippet() + ") < 0 ? 'left' : 'right'",
        Operator.CONDITIONAL.getPrecedence());
  }

  @Override
  public ImmutableSet<String> getRequiredJsLibNames() {
    return ImmutableSet.copyOf(bidiGlobalDirProvider.get().getNamespace().asSet());
  }

  @Override
  public PyExpr computeForPySrc(List<PyExpr> args) {
    BidiGlobalDir bidiGlobalDir = bidiGlobalDirProvider.get();
    return new PyExpr(
        "'left' if (" + bidiGlobalDir.getCodeSnippet() + ") < 0 else 'right'",
        PyExprUtils.pyPrecedenceForOperator(Operator.CONDITIONAL));
  }
}
