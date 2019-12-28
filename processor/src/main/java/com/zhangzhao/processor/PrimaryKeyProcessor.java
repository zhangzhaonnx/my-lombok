package com.zhangzhao.processor;

import static java.util.Collections.singletonList;

import com.google.auto.service.AutoService;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;
import com.zhangzhao.annotation.PrimaryKey;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;

//@AutoService(Processor.class)
public class PrimaryKeyProcessor extends AbstractProcessor {


  private Messager messager;
  private JavacTrees trees;
  private TreeMaker treeMaker;
  private Names names;

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return new HashSet<>(singletonList(PrimaryKey.class.getCanonicalName()));
  }

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    this.messager = processingEnv.getMessager();
    this.trees = JavacTrees.instance(processingEnv);
    Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
    this.treeMaker = TreeMaker.instance(context);
    this.names = Names.instance(context);
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(PrimaryKey.class);
    elements.forEach(element -> {
      PrimaryKey annotation = element.getAnnotation(PrimaryKey.class);
      String keyName = annotation.keyName();
      JCTree.JCExpression keyType = chainDotsString(getKeyTypeName(annotation));
      JCTree.JCVariableDecl field = treeMaker.VarDef(
          treeMaker.Modifiers(Flags.PRIVATE),
          names.fromString(keyName), keyType, null);

      JCTree.JCClassDecl jcClassDecl = (JCTree.JCClassDecl) trees.getTree(element);
      jcClassDecl.defs = jcClassDecl.defs.prepend(field);
    });

    return true;
  }

  private JCTree.JCExpression chainDotsString(String typeName) {
    String[] componentArray = typeName.split("\\.");
    JCTree.JCExpression expr = treeMaker.Ident(names.fromString(componentArray[0]));
    for (int i = 1; i < componentArray.length; i++) {
      expr = treeMaker.Select(expr, names.fromString(componentArray[i]));
    }
    return expr;
  }

  private static String getKeyTypeName(PrimaryKey annotation) {
    try {
      annotation.keyType();
    } catch (MirroredTypeException mte) {
      DeclaredType classTypeMirror = (DeclaredType) mte.getTypeMirror();
      TypeElement classTypeElement = (TypeElement) classTypeMirror.asElement();
      return classTypeElement.getQualifiedName().toString();
    }
    return null;
  }
}
