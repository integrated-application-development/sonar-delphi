unit TypeParameterNameDeclaration;

interface

type
  TFoo<T> = class
    type
      TFile = file of T;
      TType = type T;
      TPointer = ^T;
      
      TArray<Y> = array of Y;
      TProceduralType<Z> = function(Foo: Z; Bar: T): T;
      TBar<B> = record
        function Baz(Foo: TFoo<B>; Bar: TBar<T>; Baz: B): B;
      end;
    function Test<X>(Argument: T): T;
  end;

implementation

function TFoo<T>.Test<X>(Argument: T): T;
begin
  Result := Argument;
end;

end.