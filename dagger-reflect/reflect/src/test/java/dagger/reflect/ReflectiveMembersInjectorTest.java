/*
 * Copyright (C) 2018 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dagger.reflect;

import dagger.MembersInjector;
import javax.inject.Inject;
import javax.inject.Named;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static dagger.reflect.Annotations.named;
import static org.junit.Assert.fail;

@SuppressWarnings("ALL") // Unused fields/parameters and over-specified visibility for testing.
public final class ReflectiveMembersInjectorTest {
  private static class PrivateField {
    // [dagger-compiler] error: Dagger does not support injection into private fields
    @Inject private String privateField;
  }

  @Test public void privateFieldFails() {
    BindingGraph graph = new BindingGraph.Builder().build();
    try {
      ReflectiveMembersInjector.create(PrivateField.class, graph);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat()
          .startsWith("Dagger does not support injection into private fields: "
              + PrivateField.class.getCanonicalName()
              + ".privateField");
    }
  }

  private static class StaticField {
    // [dagger-compiler] error: Dagger does not support injection into static fields
    @Inject static String staticField;
  }

  @Test public void staticFieldFails() {
    BindingGraph graph = new BindingGraph.Builder().build();
    try {
      ReflectiveMembersInjector.create(StaticField.class, graph);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat()
          .startsWith("Dagger does not support injection into static fields: "
              + StaticField.class.getCanonicalName()
              + ".staticField");
    }
  }

  private static class FieldVisibilities {
    @Inject protected String one;
    @Inject Long two;
    @Inject public Integer three;
  }

  @Test public void fieldVisibilities() {
    BindingGraph graph = new BindingGraph.Builder()
        .add(Key.of(null, String.class), new Binding.Instance<>("one"))
        .add(Key.of(null, Long.class), new Binding.Instance<>(2L))
        .add(Key.of(null, Integer.class), new Binding.Instance<>(3))
        .build();
    MembersInjector<FieldVisibilities> injector =
        ReflectiveMembersInjector.create(FieldVisibilities.class, graph);
    FieldVisibilities instance = new FieldVisibilities();
    injector.injectMembers(instance);
    assertThat(instance.one).isEqualTo("one");
    assertThat(instance.two).isEqualTo(2L);
    assertThat(instance.three).isEqualTo(3);
  }

  private static class EmptyClass {}

  @Test public void emptyInjection() {
    BindingGraph graph = new BindingGraph.Builder().build();
    MembersInjector<EmptyClass> injector =
        ReflectiveMembersInjector.create(EmptyClass.class, graph);
    EmptyClass instance = new EmptyClass();

    injector.injectMembers(instance);

    // no state, nothing to verify, except it didn't throw
  }

  private static class NoInjectsClass {
    protected String one;
    Long two;
    public int three;

    private int count = 0;
    public void one(String one) {
      count++;
    }
    Long two(Long two) {
      count++;
      return two;
    }
    private void three(int three) {
      count++;
    }
  }

  @Test public void noInjection() {
    BindingGraph graph = new BindingGraph.Builder()
        .add(Key.of(null, String.class), new Binding.Instance<>("one"))
        .add(Key.of(null, Long.class), new Binding.Instance<>(2L))
        .add(Key.of(null, int.class), new Binding.Instance<>(3))
        .build();
    MembersInjector<NoInjectsClass> injector =
        ReflectiveMembersInjector.create(NoInjectsClass.class, graph);
    NoInjectsClass instance = new NoInjectsClass();

    injector.injectMembers(instance);

    assertThat(instance.one).isEqualTo(null);
    assertThat(instance.two).isEqualTo(null);
    assertThat(instance.three).isEqualTo(0);
    assertThat(instance.count).isEqualTo(0);
  }

  private static class FieldQualifier {
    @Inject @Named("tres") Long three;
  }

  @Test public void fieldQualifier() {
    BindingGraph graph = new BindingGraph.Builder()
        .add(Key.of(named("tres"), Long.class), new Binding.Instance<>(3L))
        .build();
    MembersInjector<FieldQualifier> injector =
        ReflectiveMembersInjector.create(FieldQualifier.class, graph);
    FieldQualifier instance = new FieldQualifier();
    injector.injectMembers(instance);
    assertThat(instance.three).isEqualTo(3);
  }

  private static class PrivateMethod {
    // [dagger-compiler] error: Dagger does not support injection into private methods
    @Inject private void privateMethod(String one) {}
  }

  @Test public void privateMethodFails() {
    BindingGraph graph = new BindingGraph.Builder().build();
    try {
      ReflectiveMembersInjector.create(PrivateMethod.class, graph);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat()
          .startsWith("Dagger does not support injection into private methods: "
              + PrivateMethod.class.getCanonicalName()
              + ".privateMethod()");
    }
  }

  private static class StaticMethod {
    // [dagger-compiler] error: Dagger does not support injection into static methods
    @Inject static void staticMethod(String one) {}
  }

  @Test public void staticMethodFails() {
    BindingGraph graph = new BindingGraph.Builder().build();
    try {
      ReflectiveMembersInjector.create(StaticMethod.class, graph);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat()
          .startsWith("Dagger does not support injection into static methods: "
              + StaticMethod.class.getCanonicalName()
              + ".staticMethod()");
    }
  }

  private static class MethodVisibilities {
    int count;

    @Inject protected void one(String one) {
      assertThat(one).isEqualTo("one");
      count++;
    }
    @Inject void two(Long two) {
      assertThat(two).isEqualTo(2L);
      count++;
    }
    @Inject public void three(Integer three) {
      assertThat(three).isEqualTo(3);
      count++;
    }
  }

  @Test public void methodVisibilities() {
    BindingGraph graph = new BindingGraph.Builder()
        .add(Key.of(null, String.class), new Binding.Instance<>("one"))
        .add(Key.of(null, Long.class), new Binding.Instance<>(2L))
        .add(Key.of(null, Integer.class), new Binding.Instance<>(3))
        .build();
    MembersInjector<MethodVisibilities> injector =
        ReflectiveMembersInjector.create(MethodVisibilities.class, graph);
    MethodVisibilities instance = new MethodVisibilities();
    injector.injectMembers(instance);
    assertThat(instance.count).isEqualTo(3);
  }

  private static class MethodQualifer {
    boolean called;

    @Inject protected void one(@Named("tres") Long three) {
      assertThat(three).isEqualTo(3L);
      called = true;
    }
  }

  @Test public void methodQualfier() {
    BindingGraph graph = new BindingGraph.Builder()
        .add(Key.of(named("tres"), Long.class), new Binding.Instance<>(3L))
        .build();
    MembersInjector<MethodQualifer> injector =
        ReflectiveMembersInjector.create(MethodQualifer.class, graph);
    MethodQualifer instance = new MethodQualifer();
    injector.injectMembers(instance);
    assertThat(instance.called).isTrue();
  }

  private static class MethodMultipleDependencies {
    boolean called;

    @Inject protected void multiple(String one, long two, long two2, @Named("tres") Integer three) {
      assertThat(one).isEqualTo("one");
      assertThat(two).isEqualTo(2L);
      assertThat(two2).isEqualTo(2L);
      assertThat(three).isEqualTo(3);
      called = true;
    }
  }

  @Test public void methodMultipleDependencies() {
    BindingGraph graph = new BindingGraph.Builder()
        .add(Key.of(null, String.class), new Binding.Instance<>("one"))
        .add(Key.of(null, long.class), new Binding.Instance<>(2L))
        .add(Key.of(named("tres"), Integer.class), new Binding.Instance<>(3))
        .build();
    MembersInjector<MethodMultipleDependencies> injector =
        ReflectiveMembersInjector.create(MethodMultipleDependencies.class, graph);
    MethodMultipleDependencies instance = new MethodMultipleDependencies();
    injector.injectMembers(instance);
    assertThat(instance.called).isTrue();
  }

  private static class MethodReturnTypes {
    int count;

    @Inject String one(String one) {
      count++;
      return one;
    }
    @Inject long two(Long two) {
      count++;
      return two;
    }
    @Inject void three(Integer three) {
      count++;
    }
  }

  @Test public void methodReturnTypesIgnored() {
    BindingGraph graph = new BindingGraph.Builder()
        .add(Key.of(null, String.class), new Binding.Instance<>("one"))
        .add(Key.of(null, Long.class), new Binding.Instance<>(2L))
        .add(Key.of(null, Integer.class), new Binding.Instance<>(3))
        .build();
    MembersInjector<MethodReturnTypes> injector =
        ReflectiveMembersInjector.create(MethodReturnTypes.class, graph);
    MethodReturnTypes instance = new MethodReturnTypes();
    injector.injectMembers(instance);
    assertThat(instance.count).isEqualTo(3);
  }

  private static class FieldsBeforeMethods {
    @Inject String one;
    boolean called;

    @Inject void one(String one) {
      assertThat(one).isEqualTo("one");
      called = true;
    }
  }

  @Test public void fieldInjectBeforeMethods() {
    BindingGraph graph = new BindingGraph.Builder()
        .add(Key.of(null, String.class), new Binding.Instance<>("one"))
        .build();
    MembersInjector<FieldsBeforeMethods> injector =
        ReflectiveMembersInjector.create(FieldsBeforeMethods.class, graph);
    FieldsBeforeMethods instance = new FieldsBeforeMethods();
    injector.injectMembers(instance);
    assertThat(instance.called).isTrue();
  }

  static class Base {
    @Inject String one;
    boolean baseCalled;

    @Inject void two(Long two) {
      baseCalled = true;
    }
  }
  static class Subtype extends Base {
    @Inject Long two;
    boolean subtypeCalled;

    @Inject void one(String one) {
      subtypeCalled = true;
    }
  }

  @Test public void entireHierarchyInjected() {
    BindingGraph graph = new BindingGraph.Builder()
        .add(Key.of(null, String.class), new Binding.Instance<>("one"))
        .add(Key.of(null, Long.class), new Binding.Instance<>(2L))
        .build();
    MembersInjector<Subtype> injector =
        ReflectiveMembersInjector.create(Subtype.class, graph);
    Subtype instance = new Subtype();
    injector.injectMembers(instance);
    assertThat(instance.one).isEqualTo("one");
    assertThat(instance.two).isEqualTo(2L);
    assertThat(instance.baseCalled).isTrue();
    assertThat(instance.subtypeCalled).isTrue();
  }

  private static interface Interface {
    // [dagger-compile] Methods with @Inject may not be abstract
    @Inject void interfaceMethod(String one);
  }

  @Test public void interfaceInjectionFails() {
    BindingGraph graph = new BindingGraph.Builder().build();
    try {
      ReflectiveMembersInjector.create(Interface.class, graph);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat()
          .startsWith("Methods with @Inject may not be abstract: "
              + Interface.class.getCanonicalName()
              + ".interfaceMethod()");
    }
  }

  private static abstract class Abstract {
    // [dagger-compile] Methods with @Inject may not be abstract
    @Inject abstract void abstractMethod(String one);
  }

  @Test public void abstractInjectionFails() {
    BindingGraph graph = new BindingGraph.Builder().build();
    try {
      ReflectiveMembersInjector.create(Abstract.class, graph);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat()
          .startsWith("Methods with @Inject may not be abstract: "
              + Abstract.class.getCanonicalName()
              + ".abstractMethod()");
    }
  }
}
