unit TypeSignature;

{This is a sample Delphi file.}

{$T+}

interface

  type
    TFooClass = class of TFoo;
    // Type signature of TFooClass is "class of TFoo" because TFoo is unresolved.

    PFoo = ^TFoo;
    // Type signature of TFooClass is "^TFoo" because TFoo is unresolved.

    TFoo = class(TObject);
    // Type signature of TFooClass should now be "class of TypeSignatures.TFoo" because TFoo has 
    // been found.
    // Type signature of PFoo should now be "^TypeSignatures.TFoo" because TFoo has been found.

  procedure ConsumeFooClass(FooClass: TFooClass);
  procedure ConsumeFooPointer(FooClass: PFoo);

implementation

procedure ConsumeFooClass(FooClass: TFooClass);
begin
  // Do nothing
end;

procedure ConsumeFooPointer(FooPointer: PFoo);
begin
  // Do nothing
end;

procedure Test(Foo: TFoo);
begin
  // Type signature of name occurrence "TFoo" should be "class of TypeSignatures.TFoo"
  ConsumeFooClass(TFoo);

  // Type signature of expression "@Foo" should be "^TypeSignatures.TFoo"
  ConsumeFooPointer(@Foo);
end;

end.