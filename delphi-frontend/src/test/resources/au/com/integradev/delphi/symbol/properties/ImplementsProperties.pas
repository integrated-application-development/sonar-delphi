unit ImplementsProperties;

interface

type
  IBar = interface
  end;

  TBar = class(IBar)
  end;

  TFoo = class
    property Bar: TBar implements IBar;
  end;

implementation

end.