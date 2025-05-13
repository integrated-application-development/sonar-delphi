unit Guid;

interface


const
  Foo = '{4E89BDD0-B67C-4B4D-86E6-2C0D1DC7B237';

type
  TBar = interface
    [Foo]
  end;

type
  FooAttribute = class(TCustomAttribute)
    // ...
  end;

  TBaz = interface
    [assembly : Foo, '{4E89BDD0-B67C-4B4D-86E6-2C0D1DC7B237']
    procedure Flarp;
  end;


implementation

end.