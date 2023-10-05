unit HigherScopeSuffixedAttribute;

interface

type
  FooAttribute = class(TCustomAttribute)
  end;

  TContainer = class(TObject)
  type
    Foo = class(TCustomAttribute)
    end;

    [Foo]
    TBar = class(TObject)
    end;
  end;

implementation

end.