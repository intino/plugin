package io.intino.plugin.codeinsight;

import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.FakePsiElement;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.JavaClassReferenceProvider;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JavaHelper {

	private static final Logger LOG = Logger.getInstance(JavaHelper.class.getName());

	public static JavaHelper getJavaHelper(Project project) {
		return new JavaHelper.Impl(JavaPsiFacade.getInstance(project));
	}

	@Nullable
	public PsiReferenceProvider getClassReferenceProvider() {
		return null;
	}

	@Nullable
	public NavigatablePsiElement findClass(@Nullable String className) {
		return null;
	}

	@Nullable
	public NavigationItem findPackage(@Nullable String packageName) {
		return null;
	}

	@Nullable
	public NavigatablePsiElement findClassMethod(@Nullable String className, @Nullable String methodName, int paramCount, @NotNull String... paramTypes) {
		return null;
	}


	public List<String> getMethodTypes(@Nullable NavigatablePsiElement method) {
		return Collections.singletonList("void");
	}

	public List<String> getAnnotations(@Nullable NavigatablePsiElement element) {
		return Collections.emptyList();
	}

	private static class Impl extends JavaHelper {
		private final JavaPsiFacade myFacade;

		private Impl(JavaPsiFacade facade) {
			myFacade = facade;
		}

		private static boolean isAssignable(PsiMethod method, String[] paramTypes) {
			PsiParameterList parameterList = method.getParameterList();
			if (parameterList.getParametersCount() < paramTypes.length) return false;
			PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(method.getProject());
			PsiParameter[] psiParameters = parameterList.getParameters();
			boolean result = false;
			for (int i = 0; i < paramTypes.length; i++) {
				String paramType = paramTypes[i];
				PsiParameter parameter = psiParameters[i];
				PsiType psiType = parameter.getType();
				PsiType typeFromText = elementFactory.createTypeFromText(paramType, parameter);
				result = psiType.isAssignableFrom(typeFromText);
				if (!result) break;
			}
			return result;
		}

		@Override
		public PsiReferenceProvider getClassReferenceProvider() {
			JavaClassReferenceProvider provider = new JavaClassReferenceProvider();
			provider.setSoft(false);
			return provider;
		}

		@Override
		public PsiClass findClass(String className) {
			if (className == null || className.isEmpty()) return null;
			return myFacade.findClass(className, GlobalSearchScope.projectScope(myFacade.getProject()));
		}

		@Override
		public NavigationItem findPackage(String packageName) {
			return myFacade.findPackage(packageName);
		}

		@Override
		public PsiMethod findClassMethod(@Nullable String className, @Nullable String methodName, int paramCount, @NotNull String... paramTypes) {
			PsiClass aClass = className != null ? findClass(className) : null;
			PsiMethod[] methods = aClass == null || methodName == null ? PsiMethod.EMPTY_ARRAY : aClass.findMethodsByName(methodName, true);
			for (PsiMethod method : methods) {
				if (paramCount < 0 || paramCount == method.getParameterList().getParametersCount()) {
					if (paramTypes.length > 0 && !isAssignable(method, paramTypes)) {
						continue;
					}
					return method;
				}
			}
			return ArrayUtil.getFirstElement(methods);
		}

		public List<NavigatablePsiElement> getClassMethods(String className, boolean staticMethods) {
			PsiClass aClass = findClass(className);
			if (aClass == null) return Collections.emptyList();
			final ArrayList<NavigatablePsiElement> result = new ArrayList<NavigatablePsiElement>();
			for (PsiMethod method : aClass.getAllMethods()) {
				PsiModifierList modifierList = method.getModifierList();
				if (modifierList.hasExplicitModifier(PsiModifier.PUBLIC) && staticMethods == modifierList.hasExplicitModifier(PsiModifier.STATIC)) {
					result.add(method);
				}
			}
			return result;
		}

		@Override
		public List<String> getMethodTypes(NavigatablePsiElement method) {
			if (method == null) return Collections.emptyList();
			PsiMethod psiMethod = (PsiMethod) method;
			PsiType returnType = psiMethod.getReturnType();
			List<String> strings = new ArrayList<String>();
			strings.add(returnType == null ? "" : returnType.getCanonicalText());
			for (PsiParameter parameter : psiMethod.getParameterList().getParameters()) {
				strings.add(parameter.getType().getCanonicalText());
				strings.add(parameter.getName());
			}
			return strings;
		}

		@Override
		public List<String> getAnnotations(NavigatablePsiElement element) {
			if (element == null) return Collections.emptyList();
			PsiModifierList modifierList = ((PsiModifierListOwner) element).getModifierList();
			if (modifierList == null) return super.getAnnotations(element);
			List<String> strings = new ArrayList<String>();
			for (PsiAnnotation annotation : modifierList.getAnnotations()) {
				if (annotation.getParameterList().getAttributes().length > 0) continue;
				strings.add(annotation.getQualifiedName());
			}
			return strings;
		}
	}

	public static class ReflectionHelper extends JavaHelper {
		@Nullable
		@Override
		public NavigatablePsiElement findClass(String className) {
			if (className == null) return null;
			try {
				Class<?> aClass = Class.forName(className);
				return new MyElement<Class>(aClass);
			} catch (ClassNotFoundException e) {
				LOG.info(e.getMessage(), e);
				return null;
			}
		}

		@Nullable
		@Override
		public NavigatablePsiElement findClassMethod(@Nullable String className, @Nullable String methodName, int paramCount, @NotNull String... paramTypes) {
			if (className == null) return null;
			try {
				Class<?> aClass = Class.forName(className);
				for (Method method : aClass.getDeclaredMethods()) {
					if (!method.getName().equals(methodName)) continue;
					if (paramCount < 0 || paramCount + 2 == method.getParameterTypes().length) {
						return new MyElement<>(method);
					}
				}
				return null;
			} catch (ClassNotFoundException e) {
				LOG.error(e.getMessage(), e);
				return null;
			}
		}

		@Override
		public List<String> getMethodTypes(NavigatablePsiElement method) {
			if (method == null) return Collections.emptyList();
			Method delegate = ((MyElement<Method>) method).myDelegate;
			Type[] parameterTypes = delegate.getGenericParameterTypes();
			ArrayList<String> result = new ArrayList<>(parameterTypes.length + 1);
			result.add(delegate.getGenericReturnType().toString());
			int paramCounter = 0;
			for (Type parameterType : parameterTypes) {
				result.add(parameterType.toString());
				result.add("p" + (paramCounter++));
			}
			return result;
		}

		@Override
		public List<String> getAnnotations(NavigatablePsiElement element) {
			if (element == null) return Collections.emptyList();
			AnnotatedElement delegate = ((MyElement<AnnotatedElement>) element).myDelegate;
			Annotation[] annotations = delegate.getDeclaredAnnotations();
			ArrayList<String> result = new ArrayList<>(annotations.length);
			for (Annotation annotation : annotations) {
				Class<? extends Annotation> annotationType = annotation.annotationType();
				result.add(annotationType.getCanonicalName());
			}
			return result;
		}
	}

	private static class MyElement<T> extends FakePsiElement implements NavigatablePsiElement {

		private final T myDelegate;

		MyElement(T delegate) {
			myDelegate = delegate;
		}

		@Override
		public PsiElement getParent() {
			return null;
		}
	}
}