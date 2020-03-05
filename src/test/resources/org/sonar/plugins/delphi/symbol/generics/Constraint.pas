unit Constraint;

{This is a sample Delphi file.}

interface

 type
   ISerializable = interface 
     function Clone: ISerializable; overload;
   end;

   ICloneable = interface 
     function Clone: ICloneable; overload;
   end;

   TTest = class(TInterfacedObject, ISerializable, ICloneable)
     function CloneableClone: ICloneable;
     function SerializableClone: ISerializable;
     function Clone: TTest;
     
     function ISerializable.Clone = SerializableClone;
     function ICloneable.Clone = CloneableClone;
   end;
   
   TFoo<T: ICloneable, TTest, ISerializable> = class
     FData: T;
     constructor Create(Test: TTest);
     procedure Test;
   end;

implementation

function TTest.SerializableClone: ISerializable;
begin
  Result := Self;
end;

function TTest.CloneableClone: ICloneable;
begin
  Result := Self;
end;

function TTest.Clone: TTest;
begin
  Result := Self;
end;

constructor TFoo<T>.Create(Test: TTest);
begin
  FData := Test;
end;

procedure TFoo<T>.Test;
begin
  FData.Clone;
end;

var
  Foo: TFoo<TTest>;
  Test: TTest;
  
initialization
  Foo := TFoo<TTest>.Create(Test);
  Foo.Test;
end.