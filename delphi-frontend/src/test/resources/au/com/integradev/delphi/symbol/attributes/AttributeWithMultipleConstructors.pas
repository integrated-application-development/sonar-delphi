unit AttributeWithMultipleConstructors;

interface

type
  Foo = class(TCustomAttribute)
    constructor Create; overload;
    constructor Create(MyVal: Integer); overload;
    constructor Create(MyStr: string); overload;
    constructor Create(MyVal: Integer; MySecondVal: Integer); overload;
  end;

  [Foo]
  [Foo(5)]
  [Foo('hello')]
  [Foo(10, 15)]
  TBar = class(TObject)
  end;

implementation

end.